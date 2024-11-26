(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.tilemap :as tilemap]
            [flatland.ordered.set :as oset])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Color Texture)
           (com.badlogic.gdx.graphics.g2d Animation TextureRegion)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer$ShapeType)
           (com.badlogic.gdx.scenes.scene2d Actor)
           (com.badlogic.gdx.utils Array)))

(defn sprite-array
  [txr coll]
  (Array/with (object-array (map #(-> txr (get (first %)) (get (second %))) coll))))

(defn walk-speed
  [delta]
  (* 5 delta))

(defn create-mob
  [sprite-file-name]
  (let [tx (Texture. (FileHandle. (str constants/sprites-dir sprite-file-name)))
        txr (TextureRegion/split tx
                                 constants/mob-width
                                 constants/mob-height)
        animations {:down (Animation. (float constants/walk-animation-speed)
                                      ^Array (sprite-array txr [[0 0] [0 1] [0 0] [0 2]]))
                    :up   (Animation. (float constants/walk-animation-speed)
                                      ^Array (sprite-array txr [[3 0] [3 1] [3 0] [3 2]]))
                    :left (Animation. (float constants/walk-animation-speed)
                                      ^Array (sprite-array txr [[1 0] [1 1] [1 0] [1 2]]))
                    :right (Animation. (float constants/walk-animation-speed)
                                       ^Array (sprite-array txr [[2 0] [2 1] [2 0] [2 2]]))}
        x (atom 27)
        y (atom 16)
        is-blocked? (fn [direction to-x to-y]
                      (let [fx (Math/floor ^float to-x)
                            fy (Math/floor ^float to-y)
                            cx (Math/ceil ^float to-x)
                            cy (Math/ceil ^float to-y)
                            blocked? (atom false)
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
                        (doseq [cell-coords @cells]
                          (let [layer (tilemap/get-layer @tilemap/tilemap "midground")
                                cell (.getCell layer (first cell-coords) (second cell-coords))]
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
        keys-down (atom (oset/ordered-set))
        direction (atom :down)
        key-down! (fn [key]
                    (swap! keys-down conj key)
                    true)
        key-up! (fn [key]
                  (swap! keys-down disj key)
                  true)
        state-time (atom 0)
        move (fn [direction amount]
               (cond
                 (= :up direction)
                 (if (is-blocked? :up @x (+ @y amount))
                   (swap! y (fn [current] (+ current (- (Math/ceil ^float @y) @y))))
                   (swap! y (fn [current] (+ current amount))))
                 (= :down direction)
                 (if (is-blocked? :down @x (- @y amount))
                   (swap! y (fn [current] (- current (- @y (Math/floor ^float @y)))))
                   (swap! y (fn [current] (- current amount))))
                 (= :left direction)
                 (if (is-blocked? :left (- @x amount) @y)
                   (swap! x (fn [current] (- current (- @x (Math/floor ^float @x)))))
                   (swap! x (fn [current] (- current amount))))
                 (= :right direction)
                 (if (is-blocked? :right (+ @x amount) @y)
                   (swap! x (fn [current] (+ current (- (Math/ceil ^float @x) @x))))
                   (swap! x (fn [current] (+ current amount))))))]
    {:actor (proxy [Actor] []
              (draw [batch _]
                (let [frame (.getKeyFrame (get animations @direction) @state-time true)]
                  (.draw batch
                         frame
                         (float (- (/ constants/screen-width 2) (/ constants/mob-width 2)))
                         (float (- (/ constants/screen-height 2) (/ constants/mob-height 2))))))
              (act [delta]
                (let [d1 (first @keys-down)
                      d2 (last @keys-down)]
                  (when d1
                    (move d1 (walk-speed delta))
                    (swap! direction (constantly d1))
                    (swap! state-time (fn [t] (+ t delta))))
                  (if d2
                    (move d2 (walk-speed delta))))))
     :key-down! key-down!
     :key-up! key-up!
     :x x
     :y y}))
