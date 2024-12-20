(ns cljrpgengine.tilemap
  (:require [cljrpgengine.constants :as constants])
  (:import (com.badlogic.gdx.maps.tiled TmxMapLoader)
           (com.badlogic.gdx.math Intersector Rectangle)))

(def background-layer "background")
(def midground-layer "midground")
(def foreground-layer "foreground")
(def warp-layer "warps")
(def shop-layer "shops")

(def tilemap (atom nil))

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
  [layer x y]
  (let [blocked? (atom false)]
    (if-let [cell (.getCell layer x y)]
      (let [objects (-> cell (.getTile) (.getObjects))]
        (if (= 1 (.getCount objects))
          (swap! blocked? (constantly true)))))
    @blocked?))

(defn is-blocked?
  [x y]
  (or (is-layer-blocking? (get-layer background-layer) x y)
      (is-layer-blocking? (get-layer midground-layer) x y)))

(defn get-shop
  [x y]
  (if-let [shop-layer (get-layer shop-layer)]
    (let [objects (.getObjects shop-layer)
          shop-area (Rectangle. (* x constants/tile-size)
                                (* y constants/tile-size)
                                constants/tile-size
                                constants/tile-size)]
      (if-let [shop (first (filter (fn [o]
                                     (let [p (.getProperties o)]
                                       (and (= "shop" (.get p "type"))
                                            (Intersector/overlaps shop-area (.getRectangle o)))))
                                   objects))]
        (keyword (.getName shop))))))

(defn get-entrance
  [entrance-name]
  (let [objects (.getObjects (get-layer warp-layer))
        entrance (first (filter (fn [o]
                                  (let [p (.getProperties o)]
                                    (and (= "entrance" (.get p "type"))
                                         (= (name entrance-name) (.getName o)))))
                                objects))]
    (if-let [p (.getProperties entrance)]
      {:x (/ (.get p "x") constants/tile-size)
       :y (/ (.get p "y") constants/tile-size)
       :direction (keyword (.get p "direction"))})))

(defn get-exit
  [x y]
  (let [objects (.getObjects (get-layer warp-layer))
        player (Rectangle. (* x constants/tile-size)
                           (* y constants/tile-size)
                           constants/tile-size
                           constants/tile-size)]
    (if-let [entrance (first
                       (filter
                        #(and (= "exit" (.get (.getProperties %) "type"))
                              (Intersector/overlaps player (.getRectangle %)))
                        objects))]
      {:scene (keyword (.get (.getProperties entrance) "scene"))
       :room (keyword (.get (.getProperties entrance) "room"))
       :to (keyword (.get (.getProperties entrance) "to"))})))
