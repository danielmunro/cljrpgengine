(ns cljrpgengine.map
  (:require [clojure.data.json :as json]
            [quil.core :as q])
  (:import (java.awt.image BufferedImage)
           (java.io File)
           (javax.imageio ImageIO)
           (processing.core PImage)))

(def cnt (atom 0))
(def x (atom 0))
(def y (atom 0))

(defn transform-tile
  [tile tw th iw]
  (swap! cnt inc)
  (swap! x + tw)
  (if (> @x iw)
    (do
      (swap! x (constantly 0))
      (swap! y + th)))
  {(tile "id") {:x @x
                :y @y}})

(defn load-tileset
  [area-name]
  (let [data (json/read-str (slurp (str "resources/" area-name "/" area-name "-tileset.tsj")))
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
     :tiles (into {} (map (fn [t] (transform-tile t tilewidth tileheight imagewidth)) (data "tiles")))
     }))

(defn transform-layer
  [layer]
  {(keyword (layer "name"))
   {:data (layer "data")}})

(defn transform-objects
  [object]
  ; todo
  {})

(defn load-tilemap
  [area-name]
  (let [data (json/read-str (slurp (str "resources/" area-name "/" area-name "-tilemap.tmj")))]
    {:height (data "height")
     :width (data "width")
     :layers (into {} (map #(transform-layer %) (filter #(= "tilelayer" (% "type")) (data "layers"))))
     :objects (into {} (map #(transform-objects %) (filter #(= "objectgroup" (% "type")) (data "layers"))))}))

(defn draw-layer
  [layer image w h mapw maph iw]
  (let [buf (BufferedImage. (* mapw w) (* maph h) BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics buf)]
    (loop [x 0 y 0]
      (when (< y maph)
        (let [tile (- (get-in layer [:data (+ x (* y mapw))]) 1)]
          (if (>= tile 0)
            (do
              (let [dx1 (* x w)
                    dy1 (* y h)
                    dx2 (+ dx1 w)
                    dy2 (+ dy1 h)
                    sx1 (mod (* tile w) iw)
                    sy1 (* (int (Math/floor (/ (* tile w) iw))) w)
                    sx2 (+ sx1 w)
                    sy2 (+ sy1 h)]
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
  [area-name]
  (let [tilemap (load-tilemap area-name)
        tileset (load-tileset area-name)
        image (ImageIO/read (File. (str "resources/" area-name "/" area-name ".png")))
        w (:tilewidth tileset)
        h (:tileheight tileset)
        mapw (:width tilemap)
        maph (:height tilemap)
        iw (:imagewidth tileset)]
    {:tilemap tilemap
     :tileset tileset
     :image image
     :background (draw-layer (get-in tilemap [:layers :background]) image w h mapw maph iw)
     :midground (draw-layer (get-in tilemap [:layers :midground]) image w h mapw maph iw)
     :foreground (draw-layer (get-in tilemap [:layers :foreground]) image w h mapw maph iw)}))

(defn draw
  [map]
  (q/image (:background map) 0 0)
  (q/image (:midground map) 0 0)
  (q/image (:foreground map) 0 0))
