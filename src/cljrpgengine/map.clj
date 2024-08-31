(ns cljrpgengine.map
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.util :as util]
            [clojure.data.json :as json]
            [quil.core :as q])
  (:import (java.awt.image BufferedImage)
           (java.io File)
           (javax.imageio ImageIO)
           (processing.core PImage)))

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
  [area-name]
  (let [data (-> (str "resources/" area-name "/" area-name "-tileset.tsj")
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
  (map (fn
         [object]
         (merge
          {:name (object "name")
           :type (object "type")
           :x (object "x")
           :y (object "y")
           :width (object "width")
           :height (object "height")}
          (into {} (map (fn [p] {(keyword (p "name")) (p "value")}) (object "properties")))))
       objects))

(defn- transform-arrive-at
  [objects]
  (map (fn
         [object]
         {:name (object "name")
          :x (object "x")
          :y (object "y")
          :width (object "width")
          :height (object "height")})
       objects))

(defn- transform-shops
  [objects]
  (map (fn
         [object]
         {:name (object "name")
          :x (object "x")
          :y (object "y")})
       objects))

(defn- load-tilemap
  [area-name room]
  (let [data (-> (str "resources/" area-name "/" room "/" room "-tilemap.tmj")
                 (slurp)
                 (json/read-str))
        arrive-at (util/filter-first #(= "arrive_at" (% "name")) (data "layers"))
        warps (util/filter-first #(= "warps" (% "name")) (data "layers"))
        shops (util/filter-first #(= "shops" (% "name")) (data "layers"))]
    {:height (data "height")
     :width (data "width")
     :layers (into {} (map #(transform-layer %) (filter #(= "tilelayer" (% "type")) (data "layers"))))
     :warps (transform-warps (warps "objects"))
     :arrive_at (if arrive-at
                  (transform-arrive-at (arrive-at "objects"))
                  [])
     :shops (if shops
              (transform-shops shops)
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
    (PImage. buf)))

(defn load-map
  [area-name room]
  (let [tilemap (load-tilemap area-name room)
        tileset (load-tileset area-name)
        layers (:layers tilemap)
        image (-> (str "resources/" area-name "/" area-name ".png")
                  (File.)
                  (ImageIO/read))
        w (:tilewidth tileset)
        h (:tileheight tileset)
        mapw (:width tilemap)
        maph (:height tilemap)
        iw (:imagewidth tileset)]
    {:name (keyword area-name)
     :room (keyword room)
     :tilemap tilemap
     :tileset tileset
     :image image
     :background (draw-layer (:background layers) image w h mapw maph iw (partial is-blocking? tilemap tileset))
     :midground (draw-layer (:midground layers) image w h mapw maph iw (partial is-blocking? tilemap tileset))
     :foreground (draw-layer (:foreground layers) image w h mapw maph iw (partial is-blocking? tilemap tileset))}))

(defn draw-background
  [map offset-x offset-y]
  (q/image (:background map) offset-x offset-y)
  (q/image (:midground map) offset-x offset-y))

(defn draw-foreground
  [map offset-x offset-y]
  (q/image (:foreground map) offset-x offset-y))

(defn get-warp
  [map warp-name]
  (let [warp (util/filter-first #(= (:name %) warp-name) (get-in map [:tilemap :warps]))]
    (if (not warp)
      (throw (AssertionError. (str "no warp found: " warp-name))))
    warp))

(defn get-exits
  [map]
  (filter #(= "exit" (:type %)) (get-in map [:tilemap :warps])))

(defn get-exit-warp-from-coords
  [map x y]
  (let [cw (constants/character-dimensions 0)
        ch (constants/character-dimensions 1)
        tw (get-in map [:tileset :tilewidth])
        th (get-in map [:tileset :tileheight])]
    (util/filter-first
     #(util/collision-detected?
       x y (+ x cw) (+ y ch)
       (:x %) (:y %) (+ (:x %) tw) (+ (:y %) th))
     (get-exits map))))

(defn get-entrance
  [map entrance-name]
  (util/filter-first
   #(= entrance-name (:name %))
   (filter #(= "entrance" (:type %)) (get-in map [:tilemap :warps]))))
