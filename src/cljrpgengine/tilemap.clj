(ns cljrpgengine.tilemap
  (:require [cljrpgengine.constants :as constants])
  (:import (com.badlogic.gdx.maps.tiled TmxMapLoader)
           (com.badlogic.gdx.math Rectangle)))

(def LAYER_BACKGROUND "background")
(def LAYER_MIDGROUND "midground")
(def LAYER_FOREGROUND "foreground")

(def tilemap (atom nil))

(defn get-layer
  [tiled layer]
  (.get (.getLayers tiled) layer))

(defn check-collision
  [tiled player-rect]
  #_(let [layer (get-layer tiled LAYER_MIDGROUND)]
      (doseq [x (range 0 (.getWidth layer))]
        (doseq [y (range 0 (.getHeight layer))]
          (let [cell (.getCell layer x y)]
            (if cell
              (let [objects (-> cell (.getTile) (.getObjects))]
                (if (= 1 (.getCount objects))
                  (let [map-object (.get objects 0)
                      ;rect (.getRectangle map-object)
                        ]
                    #_(.overlaps player-rect (doto (Rectangle.)
                                               (.set x1 y1 x2 y2)))
                    (if (.overlaps player-rect (doto (Rectangle.)
                                                 (.set (* x 16) (* y 16) 16 16)))
                      (println "blocked"))))))))))
  #_(let [tileset (.getTileSet (.getTileSets tiled) 0)
          playerRect (doto (Rectangle.)
                       (.set @x @y constants/tile-size (+ constants/tile-size 8)))
          can-move? (atom true)]
      (doseq [i (range 0 (.size tileset))]
        (let [tile (.getTile tileset i)]
          (if-not (nil? tile)
            (doseq [e (.getObjects tile)]
              (let [props (.getProperties e)]
                (if (and
                     props
                     (.overlaps playerRect (doto (Rectangle.)
                                             (.set (.get props "x") (.get props "y") (.get props "width") (.get props "height")))))
                  (swap! can-move? (constantly false))))))))
      #_(if @can-move?
          (do-move! direction)
          false)))

(defn load-tilemap
  [scene room]
  (let [scene-name (name scene)
        room-name (name room)]
    (swap! tilemap (constantly (.load (TmxMapLoader.)
                                      (str constants/scenes-dir scene-name "/" room-name "/" scene-name "-" room-name ".tmx"))))))
