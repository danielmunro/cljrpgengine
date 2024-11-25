(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.tilemap :as tilemap]
            [cljrpgengine.util :as util]
            [flatland.ordered.set :as oset])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Texture)
           (com.badlogic.gdx.graphics.g2d Animation TextureRegion)
           (com.badlogic.gdx.scenes.scene2d Actor)
           (com.badlogic.gdx.utils Array)))

(defn sprite-array
  [txr coll]
  (Array/with (object-array (map #(-> txr (get (first %)) (get (second %))) coll))))

(defn create-mob
  [sprite-file-name]
  (let [tx (Texture. (FileHandle. (str constants/sprites-dir sprite-file-name)))
        txr (TextureRegion/split tx
                                 constants/mob-width
                                 constants/mob-height)
        animations {:down (Animation. (float constants/walk-speed)
                                      ^Array (sprite-array txr [[0 0] [0 1] [0 0] [0 2]]))
                    :up (Animation. (float constants/walk-speed)
                                    ^Array (sprite-array txr [[3 0] [3 1] [3 0] [3 2]]))
                    :left (Animation. (float constants/walk-speed)
                                      ^Array (sprite-array txr [[1 0] [1 1] [1 0] [1 2]]))
                    :right (Animation. (float constants/walk-speed)
                                       ^Array (sprite-array txr [[2 0] [2 1] [2 0] [2 2]]))}
        x (atom 27)
        y (atom 16)
        is-blocked? (fn [direction to-x to-y]
                      (let [rx (Math/round ^float to-x)
                            ry (Math/round ^float to-y)
                            fx (Math/floor ^float to-x)
                            fy (Math/floor ^float to-y)
                            cx (Math/ceil ^float to-x)
                            cy (Math/ceil ^float to-y)
                            blocked? (atom false)
                            cells (atom [])]
                        (println "x" rx to-x)
                        (println "y" ry to-y)
                        (when (= :left direction)
                          (cond
                            (= (float ry) to-y)
                            (swap! cells conj [(dec cx) ry])
                            (< to-y ry)
                            (do (swap! cells conj [(dec cx) ry])
                                (swap! cells conj [(dec cx) (inc fy)]))
                            (< ry to-y)
                            (do (swap! cells conj [(dec cx) ry])
                                (swap! cells conj [(dec cx) (dec fy)]))))
                        (when (= :right direction)
                          (cond
                            (= (float ry) to-y)
                            (swap! cells conj [(inc fx) ry])
                            (< to-y ry)
                            (do (swap! cells conj [(inc fx) ry])
                                (swap! cells conj [(inc fx) (inc fy)]))
                            (< ry to-y)
                            (do (swap! cells conj [(inc fx) ry])
                                (swap! cells conj [(inc fx) (dec fy)]))))
                        (when (= :up direction)
                          (cond
                            (= (float rx) to-x)
                            (swap! cells conj [rx (inc fy)])
                            (< to-x rx)
                            (do (swap! cells conj [rx (inc fy)])
                                (swap! cells conj [(inc fx) (inc fy)]))
                            (< rx to-x)
                            (do (swap! cells conj [rx (inc fy)])
                                (swap! cells conj [(dec fx) (inc fy)]))))
                        (when (= :down direction)
                          (cond
                            (= (float rx) to-x)
                            (swap! cells conj [rx (dec cy)])
                            (< to-x rx)
                            (do (swap! cells conj [rx (dec cy)])
                                (swap! cells conj [(inc fx) (dec cy)]))
                            (< rx to-x)
                            (do (swap! cells conj [rx (dec cy)])
                                (swap! cells conj [(dec fx) (dec cy)]))))
                        (doseq [cell-coords @cells]
                          (let [layer (tilemap/get-layer @tilemap/tilemap "midground")
                                cell (.getCell layer (first cell-coords) (second cell-coords))]
                            (if cell
                              (let [objects (-> cell (.getTile) (.getObjects))]
                                (if (= 1 (.getCount objects))
                                  (swap! blocked? (constantly true)))))))
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
                   (swap! x (fn [current] (+ current amount)))))
               (println "x, y" @x @y))]
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
                    (move d1 (* 10 delta))
                    (swap! direction (constantly d1))
                    (swap! state-time (fn [t] (+ t delta))))
                  (if d2
                    (move d2 (* 10 delta))))))
     :key-down! key-down!
     :key-up! key-up!
     :x x
     :y y}))
