(ns cljrpgengine.map
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [cljrpgengine.util :as util]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import (java.awt.geom AffineTransform)
           (java.awt.image BufferedImage)))

(def cnt (atom 0))
(def x (atom 0))
(def y (atom 0))

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

(defn- load-tileset
  [source]
  (let [data (-> source
                 (slurp)
                 (json/read-str))
        tilewidth (data "tilewidth")
        tileheight (data "tileheight")
        imagewidth (data "imagewidth")
        imageheight (data "imageheight")]
    {:name (data "name")
     :margin (data "margin")
     :spacing (data "spacing")
     :tilecount (data "tilecount")
     :image (data "image")
     :tileheight tileheight
     :tilewidth tilewidth
     :imagewidth imagewidth
     :imageheight imageheight
     :tiles (into {} (map #(transform-tile % tilewidth tileheight imagewidth) (data "tiles")))}))

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
          (merge {:name (object "name")
                  :x (object "x")
                  :y (object "y")
                  :width (object "width")
                  :height (object "height")}))
        objects))

(defn- transform-shops
  [objects]
  (mapv (fn
          [object]
          (merge {:name (keyword (object "name"))
                  :x (object "x")
                  :y (object "y")
                  :width (object "width")
                  :height (object "height")}))
        objects))

(defn- load-tilemap
  [scene room]
  (let [data (-> (str constants/scenes-dir scene "/" room "/" scene "-" room ".tmj")
                 (slurp)
                 (json/read-str))
        arrive-at (util/filter-first #(= "arrive_at" (% "name")) (data "layers"))
        warps (util/filter-first #(= "warps" (% "name")) (data "layers"))
        shops (util/filter-first #(= "shops" (% "name")) (data "layers"))]
    {:height (data "height")
     :width (data "width")
     :tileset (get (first (data "tilesets")) "source")
     :layers (into {} (map #(transform-layer %) (filter #(= "tilelayer" (% "type")) (data "layers"))))
     :warps (transform-warps (warps "objects"))
     :arrive_at (if arrive-at
                  (transform-arrive-at (arrive-at "objects"))
                  [])
     :shops (if shops
              (transform-shops (shops "objects"))
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

(defn- draw-layer
  [layer image w h mapw maph iw is-blocking?]
  (let [buf (BufferedImage. (* mapw w) (* maph h) BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics buf)]
    (loop [x 0 y 0]
      (when (< y maph)
        (let [tile (- (get-in layer [:data (-> y
                                               (* mapw)
                                               (+ x))]) 1)]
          (if (>= tile 0)
            (let [dx1 (* x w)
                  dy1 (* y h)
                  dx2 (+ dx1 w)
                  dy2 (+ dy1 h)
                  sx1 (-> tile
                          (* w)
                          (mod iw))
                  sy1 (-> tile
                          (* w)
                          (/ iw)
                          (Math/floor)
                          (int)
                          (* w))
                  sx2 (+ sx1 w)
                  sy2 (+ sy1 h)]
              (if (and
                   constants/draw-blocking
                   (is-blocking? dx1 dy1))
                (.drawRect g dx1 dy1 dx2 dy2)
                (.drawImage g image dx1 dy1 dx2 dy2 sx1 sy1 sx2 sy2 nil))))
          (recur
           (if (< (inc x) mapw)
             (inc x)
             0)
           (if (>= (inc x) mapw)
             (inc y)
             y)))))
    buf))

(defn load-map
  [scene room]
  (let [scene-name (name scene)
        room-name (name room)
        tilemap (load-tilemap scene-name room-name)
        tileset (load-tileset (str constants/scenes-dir scene-name "/" room-name "/" (:tileset tilemap)))
        image (util/load-image (str constants/tilesets-dir (:image tileset)))
        layers (:layers tilemap)
        w (:tilewidth tileset)
        h (:tileheight tileset)
        mapw (:width tilemap)
        maph (:height tilemap)
        iw (:imagewidth tileset)]
    (log/info (format "loading map :: %s - %s" scene room))
    (log/debug (format "map warps :: %s" (str/join ", " (map #(format "%s - %s - %s" (:scene %) (:room %) (:to %)) (filter #(= :exit (:type %)) (:warps tilemap))))))
    {:tilemap tilemap
     :tileset tileset
     :scene (keyword scene)
     :room (keyword room)
     :background (draw-layer (:background layers) image w h mapw maph iw (partial is-blocking? tilemap tileset))
     :midground (draw-layer (:midground layers) image w h mapw maph iw (partial is-blocking? tilemap tileset))
     :foreground (draw-layer (:foreground layers) image w h mapw maph iw (partial is-blocking? tilemap tileset))}))

(defn draw-background
  [g map offset-x offset-y]
  (let [transform (AffineTransform.)]
    (.translate transform offset-x offset-y)
    (.drawImage g (:background map) transform nil)
    (.drawImage g (:midground map) transform nil)))

(defn draw-foreground
  [g map offset-x offset-y]
  (let [transform (AffineTransform.)]
    (.translate transform offset-x offset-y)
    (.drawImage g (:foreground map) transform nil)))

(defn get-warp
  [map warp-name]
  (let [warp (util/filter-first #(= (:name %) warp-name) (get-in map [:tilemap :warps]))]
    (if (not warp)
      (throw (AssertionError. (str "no warp found: " warp-name))))
    warp))

(defn get-interaction-from-coords
  [map interaction x y]
  (let [cw (first constants/character-dimensions)
        ch (second constants/character-dimensions)]
    (util/filter-first
     #(util/collision-detected?
       x y (+ x cw) (+ y ch)
       (:x %) (:y %) (+ (:x %) (:width %)) (+ (:y %) (:height %)))
     (interaction map))))

(defn get-entrance
  [map entrance-name]
  (util/filter-first
   #(= entrance-name (:name %))
   (filter #(= :entrance (:type %)) (get-in map [:tilemap :warps]))))

(defn init-map
  [state]
  (let [map (:map @state)
        {:keys [x y direction]} (get-warp map "start")]
    (dosync
     (alter state update-in [:player] assoc
            :x x
            :y y
            :x-offset 0
            :y-offset 0
            :direction direction))))
