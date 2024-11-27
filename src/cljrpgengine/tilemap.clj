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

(defn get-layer
  [layer]
  (.get (.getLayers @tilemap) layer))

(defn load-tilemap
  [scene room]
  (let [scene-name (name scene)
        room-name (name room)]
    (swap! tilemap (constantly (.load (TmxMapLoader.)
                                      (str constants/scenes-dir scene-name "/" room-name "/" scene-name "-" room-name ".tmx"))))))

(defn- is-layer-blocking?
  [layer cells]
  (let [blocked? (atom false)]
    (doseq [cell-coords cells]
      (let [cell (.getCell layer (first cell-coords) (second cell-coords))]
        (if cell
          (let [objects (-> cell (.getTile) (.getObjects))]
            (.begin @deps/shape ShapeRenderer$ShapeType/Filled)
            (.setColor @deps/shape Color/BLUE)
            (.rect @deps/shape (first cell-coords) (second cell-coords) 1 1)
            (.end @deps/shape)
            (if (= 1 (.getCount objects))
              (swap! blocked? (constantly true)))))
        (do (.begin @deps/shape ShapeRenderer$ShapeType/Line)
            (.setColor @deps/shape Color/BLUE)
            (.rect @deps/shape (first cell-coords) (second cell-coords) 1 1)
            (.end @deps/shape))))
    @blocked?))

(defn is-blocked?
  [direction to-x to-y]
  (let [fx (Math/floor ^float to-x)
        fy (Math/floor ^float to-y)
        cx (Math/ceil ^float to-x)
        cy (Math/ceil ^float to-y)
        cells (atom [])]
    (when (= :up direction)
      (swap! cells conj [fx (inc fy)])
      (swap! cells conj [cx (inc fy)]))
    (when (= :down direction)
      (swap! cells conj [fx (dec cy)])
      (swap! cells conj [cx (dec cy)]))
    (when (= :left direction)
      (swap! cells conj [(dec cx) fy])
      (swap! cells conj [(dec cx) cy]))
    (when (= :right direction)
      (swap! cells conj [(inc fx) fy])
      (swap! cells conj [(inc fx) cy]))
    (or (is-layer-blocking? (get-layer LAYER_BACKGROUND) @cells)
        (is-layer-blocking? (get-layer LAYER_MIDGROUND) @cells))))
