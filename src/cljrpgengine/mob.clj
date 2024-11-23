(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.util :as util]
            [clojure.java.io :as io])
  (:import (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Texture)
           (com.badlogic.gdx.graphics.g2d Animation Animation$PlayMode TextureRegion)
           (com.badlogic.gdx.scenes.scene2d Actor EventListener InputListener)
           (com.badlogic.gdx.utils Array)))

(defn sprite-array
  [txr coll]
  (Array/with (object-array (map #(-> txr (get (first %)) (get (second %))) coll))))

(defn create-mob
  [sprite-file-name]
  (let [tx (Texture. (FileHandle. (io/file (str constants/sprites-dir sprite-file-name))))
        txr (TextureRegion/split tx
                                 constants/mob-width
                                 constants/mob-height)
        down (Animation. constants/walk-speed
                         (sprite-array txr [[0 0] [0 1] [0 0] [0 2]])
                         Animation$PlayMode/LOOP)
        x (atom 0)
        y (atom 0)
        move (atom {:up false
                    :down false
                    :left false
                    :right false})
        do-move! (fn [to]
                   (if (get @move to)
                     (swap! move (fn [m] (assoc m (util/opposite-direction to) false))))
                   (swap! move (fn [m] (assoc m to true)))
                   true)
        stop-move! (fn [to]
                     (swap! move (fn [m] (assoc m to false)))
                     true)]
    {:actor (proxy [Actor] []
             (draw [batch _]
               (let [frame (.getKeyFrame down @deps/state-time true)]
                 (.draw batch
                        frame
                        (float (- (/ constants/screen-width 2) (/ constants/mob-width 2)))
                        (float (- (/ constants/screen-height 2) (/ constants/mob-height 2))))))
             (act [delta]
               (cond
                 (:up @move)
                 (swap! y (fn [i] (- i (* 5 delta))))
                 (:down @move)
                 (swap! y (fn [i] (+ i (* 5 delta))))
                 (:left @move)
                 (swap! x (fn [i] (- i (* 5 delta))))
                 (:right @move)
                 (swap! x (fn [i] (+ i (* 5 delta)))))
               (println @x @y)))
     :do-move! do-move!
     :stop-move! stop-move!}))
