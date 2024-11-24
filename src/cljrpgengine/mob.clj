(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.util :as util])
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
        move (atom {:up false
                    :down false
                    :left false
                    :right false})
        direction (atom :down)
        do-move! (fn [to]
                   (if (get @move to)
                     (swap! move (fn [m] (assoc m (util/opposite-direction to) false))))
                   (swap! move (fn [m] (assoc m to true)))
                   (swap! direction (constantly to))
                   true)
        stop-move! (fn [to]
                     (swap! move (fn [m] (assoc m to false)))
                     true)
        state-time (atom 0)
        modify-location (fn [to-x to-y delta]
                          (swap! x (constantly to-x))
                          (swap! y (constantly to-y))
                          (swap! state-time (fn [t] (+ t delta))))]
    {:actor (proxy [Actor] []
             (draw [batch _]
               (let [frame (.getKeyFrame (get animations @direction) @state-time true)]
                 (.draw batch
                        frame
                        (float (- (/ constants/screen-width 2) (/ constants/mob-width 2)))
                        (float (- (/ constants/screen-height 2) (/ constants/mob-height 2))))))
             (act [delta]
               (cond
                 (:up @move)
                 (modify-location @x (+ @y (* 10 delta)) delta)
                 (:down @move)
                 (modify-location @x (- @y (* 10 delta)) delta)
                 (:left @move)
                 (modify-location (- @x (* 10 delta)) @y delta)
                 (:right @move)
                 (modify-location (+ @x (* 10 delta)) @y delta))))
     :do-move! do-move!
     :stop-move! stop-move!
     :x x
     :y y}))
