(ns cljrpgengine.map
  (:require [clojure.data.json :as json]
           [quil.core :as q]))

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

(def create-graphics (memoize (fn [w h] (q/create-graphics w h))))

(defn draw-layer
  [layer image w h mapw maph iw]
  (let [g (create-graphics w h)]
    (loop [x 0 y 0]
      (when (< y maph)
        (let [tile (- (get-in layer [:data (+ x (* y mapw))]) 1)]
          (if (>= tile 0)
            (do
              (q/with-graphics g
                               (.clear g)
                               (q/image image (-> w
                                                  (* tile)
                                                  (mod iw)
                                                  (-))
                                              (-> w
                                                  (* tile)
                                                  (/ iw)
                                                  (Math/floor)
                                                  (int)
                                                  (* w)
                                                  (-))))
              (q/image g (* x w) (* y h) w h)))
          (recur
            (if (< (inc x) mapw)
              (inc x)
              0)
            (if (>= (inc x) mapw)
              (inc y)
              y)))))))

(defn load-map
  [area-name]
  (let [tilemap (load-tilemap area-name)
        tileset (load-tileset area-name)
        image (q/load-image (str "resources/" area-name "/" area-name ".png"))
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
  ;(draw-layer (get-in map [:tilemap :layers :background]) image w h mapw maph iw)
  ;(draw-layer (get-in map [:tilemap :layers :midground]) image w h mapw maph iw)
  ;(draw-layer (get-in map [:tilemap :layers :foreground]) image w h mapw maph iw)
  (q/image (:background map) 0 0)
  (q/image (:midground map) 0 0)
  (q/image (:foreground map) 0 0))
