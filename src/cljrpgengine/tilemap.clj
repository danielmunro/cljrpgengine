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
            #_(.begin @deps/shape ShapeRenderer$ShapeType/Filled)
            #_(.setColor @deps/shape Color/BLUE)
            #_(.rect @deps/shape (first cell-coords) (second cell-coords) 1 1)
            #_(.end @deps/shape)
            (if (= 1 (.getCount objects))
              (swap! blocked? (constantly true)))))
        #_(do (.begin @deps/shape ShapeRenderer$ShapeType/Line)
              (.setColor @deps/shape Color/BLUE)
              (.rect @deps/shape (first cell-coords) (second cell-coords) 1 1)
              (.end @deps/shape))))
    @blocked?))

(defn get-next-coords
  [direction start destination]
  (let [{to-x :x to-y :y} destination
        {from-x :x from-y :y} start
        fx (Math/floor ^float to-x)
        fy (Math/floor ^float to-y)
        cx (Math/ceil ^float to-x)
        cy (Math/ceil ^float to-y)
        cells (atom [])
        blocked-move (atom nil)]
    (when (= :up direction)
      (swap! cells conj [fx (inc fy)])
      (swap! cells conj [cx (inc fy)])
      (swap! blocked-move (constantly {:x from-x
                                       :y (+ from-y (- (Math/ceil ^float from-y) from-y))})))
    (when (= :down direction)
      (swap! cells conj [fx (dec cy)])
      (swap! cells conj [cx (dec cy)])
      (swap! blocked-move (constantly {:x from-x
                                       :y (- from-y (- from-y (Math/floor ^float from-y)))})))
    (when (= :left direction)
      (swap! cells conj [(dec cx) fy])
      (swap! cells conj [(dec cx) cy])
      (swap! blocked-move (constantly {:x (- from-x (- from-x (Math/floor ^float from-x)))
                                       :y from-y})))
    (when (= :right direction)
      (swap! cells conj [(inc fx) fy])
      (swap! cells conj [(inc fx) cy])
      (swap! blocked-move (constantly {:x (+ from-x (- (Math/ceil ^float from-x) from-x))
                                       :y from-y})))
    (if (or (is-layer-blocking? (get-layer LAYER_BACKGROUND) @cells)
            (is-layer-blocking? (get-layer LAYER_MIDGROUND) @cells))
      @blocked-move
      destination)))
