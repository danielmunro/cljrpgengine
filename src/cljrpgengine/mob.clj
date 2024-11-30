(ns cljrpgengine.mob
  (:require [cljrpgengine.animation :as animation]
            [cljrpgengine.constants :as constants]
            [flatland.ordered.set :as oset])
  (:import (com.badlogic.gdx.scenes.scene2d Actor)))

(defn create-mob
  [mob-type]
  (let [animations (animation/create-from-type mob-type)
        x (atom 0)
        y (atom 0)
        keys-down (atom (oset/ordered-set))
        direction (atom :down)
        key-down! (fn [key]
                    (swap! keys-down conj key)
                    true)
        key-up! (fn [key]
                  (swap! keys-down disj key)
                  true)
        state-time (atom 0)
        add-time-delta! (fn [delta] (swap! state-time (fn [t] (+ t delta))))]
    {:actor (proxy [Actor] []
              (draw [batch _]
                (let [frame (.getKeyFrame (get animations @direction) @state-time true)]
                  (.draw batch
                         frame
                         (float (- (/ constants/screen-width 2) (/ constants/mob-width 2)))
                         (float (- (/ constants/screen-height 2) (/ constants/mob-height 2))))))
              (act [delta]))
     :key-down! key-down!
     :key-up! key-up!
     :x x
     :y y
     :direction direction
     :keys-down keys-down
     :add-time-delta! add-time-delta!
     :state-time state-time}))
