(ns cljrpgengine.tilemap
  (:require [cljrpgengine.constants :as constants])
  (:import (com.badlogic.gdx.maps.tiled TmxMapLoader)
           (com.badlogic.gdx.math Rectangle)))

(def LAYER_BACKGROUND "background")
(def LAYER_MIDGROUND "midground")
(def LAYER_FOREGROUND "foreground")

(defn create-collision-layer
  [tiled]
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
        room-name (name room)
        tiled (.load (TmxMapLoader.)
                     (str constants/scenes-dir scene-name "/" room-name "/" scene-name "-" room-name ".tmx"))]
    {:tiled tiled}))

(defn get-layer
  [tiled layer]
  (.get (.getLayers tiled) layer))
