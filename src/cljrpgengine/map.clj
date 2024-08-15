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
     :tiles (into {} (map #(transform-tile % tilewidth tileheight imagewidth) (data "tiles")))
     }))

(defn transform-layer
  [layer]
  {(keyword (layer "name"))
   {:data (layer "data")}})

(defn transform-warps
  [objects tile-size]
  (map (fn
         [object]
         (merge
           {:name (object "name")
            :x (/ (object "x") tile-size)
            :y (/ (object "y") tile-size)
            :width (object "width")
            :height (object "height")}
           (into {} (map (fn [p] {(keyword (p "name")) (p "value")}) (object "properties")))))
       objects))

(defn transform-arrive-at
  [objects tile-size]
  (map (fn
         [object]
         (merge
           {:name (object "name")
            :x (/ (object "x") tile-size)
            :y (/ (object "y") tile-size)
            :width (object "width")
            :height (object "height")}))
       objects))

(defn load-tilemap
  [area-name tile-size]
  (let [data (-> (str "resources/" area-name "/" area-name "-tilemap.tmj")
                 (slurp)
                 (json/read-str))]
    {:height (data "height")
     :width (data "width")
     :layers (into {} (map #(transform-layer %) (filter #(= "tilelayer" (% "type")) (data "layers"))))
     :warps (transform-warps ((first (filter #(= "warps" (% "name")) (data "layers"))) "objects") tile-size)
     :arrive_at (transform-arrive-at ((first (filter #(= "arrive_at" (% "name")) (data "layers"))) "objects") tile-size)}))

(defn draw-layer
  [layer image w h mapw maph iw]
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
              (.drawImage g image dx1 dy1 dx2 dy2 sx1 sy1 sx2 sy2 nil)))
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
  (let [tileset (load-tileset area-name)
        tilemap (load-tilemap area-name (:tilewidth tileset))
        layers (:layers tilemap)
        image (-> (str "resources/" area-name "/" area-name ".png")
                  (File.)
                  (ImageIO/read))
        w (:tilewidth tileset)
        h (:tileheight tileset)
        mapw (:width tilemap)
        maph (:height tilemap)
        iw (:imagewidth tileset)]
    {:tilemap tilemap
     :tileset tileset
     :image image
     :background (draw-layer (:background layers) image w h mapw maph iw)
     :midground (draw-layer (:midground layers) image w h mapw maph iw)
     :foreground (draw-layer (:foreground layers) image w h mapw maph iw)}))

(defn draw-background
  [map]
  (q/image (:background map) 0 0)
  (q/image (:midground map) 0 0))

(defn draw-foreground
  [map]
  (q/image (:foreground map) 0 0))
