(ns cljrpgengine.tilemap
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.chest :as chest]
            [cljrpgengine.log :as log]
            [cljrpgengine.util :as util]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.awt.geom AffineTransform)
           (java.awt.image BufferedImage)))

(def cnt (atom 0))
(def x (atom 0))
(def y (atom 0))
(def tilemap (atom nil))
(def opened-chests (atom #{}))

(defn- transform-tile
  [tile tw th iw]
  (swap! cnt inc)
  (swap! x + tw)
  (if (> @x iw)
    (do
      (swap! x (constantly 0))
      (swap! y + th)))
  {(tile "id") {:x @x
                :y @y}})

(defn- load-tileset-meta
  [tileset-file]
  (let [meta-filename (str constants/tilesets-dir (util/remove-file-extension tileset-file) "/meta.edn")
        file (io/file meta-filename)]
    (if (.exists file)
      (-> meta-filename
          (slurp)
          (read-string))
      {})))

(defn- load-tiled-tileset
  [source]
  (let [data (-> (str constants/tilesets-dir source)
                 (slurp)
                 (json/read-str))
        tilewidth (data "tilewidth")
        tileheight (data "tileheight")
        imagewidth (data "imagewidth")
        imageheight (data "imageheight")]
    (merge
     {:name (data "name")
      :margin (data "margin")
      :spacing (data "spacing")
      :tilecount (data "tilecount")
      :image (data "image")
      :tileheight tileheight
      :tilewidth tilewidth
      :imagewidth imagewidth
      :imageheight imageheight
      :tiles (into {} (map #(transform-tile % tilewidth tileheight imagewidth) (data "tiles")))}
     (load-tileset-meta source))))

(defn- transform-layer
  [layer]
  {(keyword (layer "name"))
   {:data (layer "data")}})

(defn- transform-warps
  [objects]
  (mapv (fn
          [object]
          (merge
           {:name (object "name")
            :type (keyword (object "type"))
            :x (object "x")
            :y (object "y")
            :width (object "width")
            :height (object "height")}
           (into {} (map (fn [p] {(keyword (p "name")) (p "value")}) (object "properties")))))
        objects))

(defn- transform-arrive-at
  [objects]
  (mapv (fn
          [object]
          {:name (object "name")
           :x (object "x")
           :y (object "y")
           :width (object "width")
           :height (object "height")})
        objects))

(defn- transform-shops
  [objects]
  (mapv (fn
          [object]
          {:name (keyword (object "name"))
           :x (object "x")
           :y (object "y")
           :width (object "width")
           :height (object "height")})
        objects))

(defn- transform-encounters
  [objects]
  (mapv (fn
          [object]
          (merge {:name (keyword (object "name"))
                  :x (object "x")
                  :y (object "y")
                  :width (object "width")
                  :height (object "height")}
                 (into {} (map (fn [p] {(keyword (p "name")) (p "value")}) (object "properties")))))
        objects))

(defn- transform-doors
  [objects]
  (mapv (fn
          [object]
          {:type (keyword (object "type"))
           :x (object "x")
           :y (object "y")
           :status (keyword (get object "status" "closed"))
           :locked (get object "locked" false)
           :key (get object "key" nil)})
        objects))

(defn- transform-chests
  [objects]
  (mapv (fn
          [object]
          (merge
           {:id (object "id")
            :x (object "x")
            :y (object "y")
            :width (object "width")
            :height (object "height")}
           (into {} (map (fn [p] {(keyword (p "name")) (p "value")}) (object "properties")))))
        objects))

(defn- load-tiled-tilemap
  [scene-name room-name]
  (let [data (-> (str constants/scenes-dir scene-name "/" room-name "/" scene-name "-" room-name ".tmj")
                 (slurp)
                 (json/read-str))
        layers (data "layers")
        arrive-at (util/filter-first #(= "arrive_at" (% "name")) layers)
        warps (util/filter-first #(= "warps" (% "name")) layers)
        shops (util/filter-first #(= "shops" (% "name")) layers)
        doors (util/filter-first #(= "doors" (% "name")) layers)
        encounters (util/filter-first #(= "encounters" (% "name")) layers)
        chests (util/filter-first #(= "chests" (% "name")) layers)]
    {:height (data "height")
     :width (data "width")
     :tileset (get (first (data "tilesets")) "source")
     :layers (into {} (map #(transform-layer %) (filter #(= "tilelayer" (% "type")) (data "layers"))))
     :warps (transform-warps (warps "objects"))
     :doors (if doors
              (transform-doors (doors "objects"))
              [])
     :arrive_at (if arrive-at
                  (transform-arrive-at (arrive-at "objects"))
                  [])
     :shops (if shops
              (transform-shops (shops "objects"))
              [])
     :encounters (if encounters
                   (transform-encounters (encounters "objects"))
                   [])
     :chests (if chests
               (transform-chests (chests "objects"))
               [])}))

(defn is-blocking?
  [tile-map tile-set x y]
  (let [map-width (:width tile-map)
        tile-height (:tileheight tile-set)
        tile-width (:tilewidth tile-set)
        mapx (/ x tile-width)
        mapy (/ y tile-height)
        tile-pos (+ mapx (* mapy map-width))
        back-tile (dec (get-in tile-map [:layers :background :data tile-pos]))
        mid-tile (dec (get-in tile-map [:layers :midground :data tile-pos]))]
    (or
     (get-in tile-set [:tiles back-tile])
     (get-in tile-set [:tiles mid-tile]))))

(defn- get-tile-coords
  ([tile image-width]
   {:x (-> tile
           (* constants/tile-size)
           (mod image-width))
    :y (-> tile
           (* constants/tile-size)
           (/ image-width)
           (Math/floor)
           (int)
           (* constants/tile-size))})
  ([tile]
   (get-tile-coords tile (-> @tilemap :tileset :imagewidth))))

(defn- draw-layer
  [layer image w h mapw maph iw is-blocking?]
  (let [buf (BufferedImage. (* mapw w) (* maph h) BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics buf)]
    (loop [x 0 y 0]
      (when (< y maph)
        (let [tile (dec (get-in layer [:data (-> y
                                                 (* mapw)
                                                 (+ x))]))]
          (if (>= tile 0)
            (let [dx1 (* x w)
                  dy1 (* y h)
                  dx2 (+ dx1 w)
                  dy2 (+ dy1 h)
                  {:keys [x y]} (get-tile-coords tile iw)
                  sx2 (+ x w)
                  sy2 (+ y h)]
              (if (and
                   constants/draw-blocking
                   (is-blocking? dx1 dy1))
                (.drawRect g dx1 dy1 dx2 dy2)
                (.drawImage g image dx1 dy1 dx2 dy2 x y sx2 sy2 nil))))
          (recur
           (if (< (inc x) mapw)
             (inc x)
             0)
           (if (>= (inc x) mapw)
             (inc y)
             y)))))
    buf))

(defn load-tilemap!
  [scene-key room-key]
  (let [scene-name (name scene-key)
        room-name (name room-key)
        tiled-tilemap (load-tiled-tilemap scene-name room-name)
        tileset-file (.getName (io/file (str constants/scenes-dir scene-name "/" room-name "/" (:tileset tiled-tilemap))))
        tiled-tileset (load-tiled-tileset tileset-file)
        image (util/load-image (str constants/tilesets-dir (:image tiled-tileset)))
        layers (:layers tiled-tilemap)
        w (:tilewidth tiled-tileset)
        h (:tileheight tiled-tileset)
        mapw (:width tiled-tilemap)
        maph (:height tiled-tilemap)
        iw (:imagewidth tiled-tileset)]
    (log/info (format "loading map :: %s - %s" scene-key room-key))
    (log/debug (format "map warps :: %s" (str/join ", " (map #(format "%s - %s - %s" (:scene %) (:room %) (:to %)) (filter #(= :exit (:type %)) (:warps tiled-tilemap))))))
    (swap! tilemap (constantly
                    {:tilemap tiled-tilemap
                     :tileset tiled-tileset
                     :tileset-image image
                     :scene scene-key
                     :room room-key
                     :background (draw-layer (:background layers) image w h mapw maph iw (partial is-blocking? tiled-tilemap tiled-tileset))
                     :midground (draw-layer (:midground layers) image w h mapw maph iw (partial is-blocking? tiled-tilemap tiled-tileset))
                     :foreground (draw-layer (:foreground layers) image w h mapw maph iw (partial is-blocking? tiled-tilemap tiled-tileset))}))))

(defn- draw-chests
  [g transform]
  (let [{:keys [tileset-image tileset]} @tilemap
        {:keys [chests]} tileset
        {:keys [width height]} (:tilemap @tilemap)
        tmp (BufferedImage. (* width constants/tile-size)
                            (* height constants/tile-size)
                            BufferedImage/TYPE_INT_ARGB)
        tmp-graphics (.getGraphics tmp)]
    (doseq [chest (->> @tilemap
                       :tilemap
                       :chests)]
      (let [status (if (contains? @opened-chests (chest/chest-key chest)) :opened :closed)
            tile (dec (-> chests :default status))
            dx1 (:x chest)
            dy1 (:y chest)
            dx2 (+ (:x chest) constants/tile-size)
            dy2 (+ (:y chest) constants/tile-size)
            {:keys [x y]} (get-tile-coords tile)
            sx2 (+ x constants/tile-size)
            sy2 (+ y constants/tile-size)]
        (.drawImage tmp-graphics tileset-image dx1 dy1 dx2 dy2 x y sx2 sy2 nil)))
    (.drawImage g tmp transform nil)))

(defn- draw-doors
  [g transform status]
  (let [{:keys [tileset-image tileset]} @tilemap
        {:keys [doors]} tileset
        {:keys [width height]} (:tilemap @tilemap)
        tmp (BufferedImage. (* width constants/tile-size)
                            (* height constants/tile-size)
                            BufferedImage/TYPE_INT_ARGB)
        tmp-graphics (.getGraphics tmp)]
    (doseq [door (->> @tilemap
                      :tilemap
                      :doors
                      (filter #(= status (:status %))))]
      (let [tile (dec (get (get doors (:type door)) status))
            dx1 (:x door)
            dy1 (:y door)
            dx2 (+ (:x door) constants/tile-size)
            dy2 (+ (:y door) constants/tile-size)
            {:keys [x y]} (get-tile-coords tile)
            sx2 (+ x constants/tile-size)
            sy2 (+ y constants/tile-size)]
        (.drawImage tmp-graphics tileset-image dx1 dy1 dx2 dy2 x y sx2 sy2 nil)))
    (.drawImage g tmp transform nil)))

(defn draw-background
  [g offset-x offset-y]
  (let [transform (AffineTransform.)
        {:keys [background midground]} @tilemap]
    (.translate transform offset-x offset-y)
    (.drawImage g background transform nil)
    (.drawImage g midground transform nil)
    (draw-doors g transform :closed)
    (draw-chests g transform)))

(defn draw-foreground
  [g offset-x offset-y]
  (let [transform (AffineTransform.)]
    (.translate transform offset-x offset-y)
    (.drawImage g (:foreground @tilemap) transform nil)
    (draw-doors g transform :opened)))

(defn get-warp
  [warp-name]
  (let [warp (util/filter-first #(= (:name %) warp-name) (get-in @tilemap [:tilemap :warps]))]
    (if (not warp)
      (throw (AssertionError. (str "no warp found: " warp-name))))
    warp))

(defn get-interaction-from-coords
  [interaction x y]
  (let [cw (first constants/character-dimensions)
        ch (second constants/character-dimensions)]
    (util/filter-first
     #(util/collision-detected?
       x y (+ x cw) (+ y ch)
       (:x %) (:y %) (+ (:x %) (:width %)) (+ (:y %) (:height %)))
     (interaction @tilemap))))

(defn get-entrance
  [entrance-name]
  (util/filter-first
   #(= entrance-name (:name %))
   (filter #(= :entrance (:type %)) (get-in @tilemap [:tilemap :warps]))))
