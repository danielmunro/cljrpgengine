(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.tilemap :as tilemap]
            [flatland.ordered.set :as oset])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Texture)
           (com.badlogic.gdx.graphics.g2d Animation TextureRegion)
           (com.badlogic.gdx.scenes.scene2d Actor)
           (com.badlogic.gdx.utils Array)))

(def MOVE_AMOUNT 1/10)

(defn sprite-array
  [txr coll]
  (Array/with (object-array (map #(-> txr (get (first %)) (get (second %))) coll))))

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
        keys-down (atom (oset/ordered-set))
        direction (atom :down)
        key-down! (fn [key]
                    (swap! keys-down conj key)
                    true)
        key-up! (fn [key]
                  (swap! keys-down disj key)
                  true)
        state-time (atom 0)
        move (fn [direction]
               (cond
                 (= :up direction)
                 (when-not (tilemap/is-blocked? :up @x (+ @y MOVE_AMOUNT))
                   (swap! y #(+ % MOVE_AMOUNT)))
                 (= :down direction)
                 (when-not (tilemap/is-blocked? :down @x (- @y MOVE_AMOUNT))
                   (swap! y #(- % MOVE_AMOUNT)))
                 (= :left direction)
                 (when-not (tilemap/is-blocked? :left (- @x MOVE_AMOUNT) @y)
                   (swap! x #(- % MOVE_AMOUNT)))
                 (= :right direction)
                 (when-not (tilemap/is-blocked? :right (+ @x MOVE_AMOUNT) @y)
                   (swap! x #(+ % MOVE_AMOUNT)))))]
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
                    (move d1)
                    (swap! direction (constantly d1))
                    (swap! state-time (fn [t] (+ t delta))))
                  (if d2
                    (move d2)))))
     :key-down! key-down!
     :key-up! key-up!
     :x x
     :y y}))
