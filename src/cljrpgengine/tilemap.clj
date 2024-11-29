(ns cljrpgengine.tilemap
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps])
  (:import (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer$ShapeType)
           (com.badlogic.gdx.maps.tiled TmxMapLoader)))

(def LAYER_BACKGROUND "background")
(def LAYER_MIDGROUND "midground")
(def LAYER_FOREGROUND "foreground")

(def tilemap (atom nil))
(def blocking-layer (atom []))

(defn get-layer
  [layer]
  (.get (.getLayers @tilemap) layer))

(defn load-tilemap
  [scene room]
  (let [scene-name (name scene)
        room-name (name room)]
    (swap! tilemap (constantly
                    (.load (TmxMapLoader.)
                           (str constants/scenes-dir scene-name "/" room-name "/" scene-name "-" room-name ".tmx"))))))

(defn- is-layer-blocking?
  [layer coordinates]
  (let [blocked? (atom false)]
    (if-let [cell (.getCell layer (first coordinates) (second coordinates))]
      (let [objects (-> cell (.getTile) (.getObjects))]
        ;(.begin @deps/shape ShapeRenderer$ShapeType/Filled)
        ;(.setColor @deps/shape Color/BLUE)
        ;(.rect @deps/shape (first coordinates) (second coordinates) 1 1)
        ;(.end @deps/shape)
        (if (= 1 (.getCount objects))
          (swap! blocked? (constantly true))))
      #_(do (.begin @deps/shape ShapeRenderer$ShapeType/Line)
            (.setColor @deps/shape Color/BLUE)
            (.rect @deps/shape (first coordinates) (second coordinates) 1 1)
            (.end @deps/shape)))
    @blocked?))

(defn is-blocked?
  [coordinates]
  (or (is-layer-blocking? (get-layer LAYER_BACKGROUND) coordinates)
      (is-layer-blocking? (get-layer LAYER_MIDGROUND) coordinates)))
