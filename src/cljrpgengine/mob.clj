(ns cljrpgengine.mob
  (:require [cljrpgengine.animation :as animation]
            [flatland.ordered.set :as oset])
  (:import (com.badlogic.gdx.scenes.scene2d Actor)))

(defn create-mob
  [identifier name starting-direction x y mob-type]
  (let [animations (animation/create-from-type mob-type)
        keys-down (atom (oset/ordered-set))
        direction (atom starting-direction)
        key-down! (fn [key]
                    (swap! keys-down conj key)
                    true)
        key-up! (fn [key]
                  (swap! keys-down disj key)
                  true)
        state-time (atom 0)
        add-time-delta! (fn [delta] (swap! state-time (fn [t] (+ t delta))))]
    {:actor (doto (proxy [Actor] []
                    (draw [batch _]
                      (let [frame (.getKeyFrame (get animations @direction) @state-time true)]
                        (.draw batch
                               frame
                               (proxy-super getX)
                               (proxy-super getY)
                               (float 1)
                               (float 1.5)))))
              (.setX x)
              (.setY y)
              (.setWidth 1)
              (.setHeight 1)
              (.setName name))
     :identifier identifier
     :name name
     :key-down! key-down!
     :key-up! key-up!
     :direction direction
     :keys-down keys-down
     :add-time-delta! add-time-delta!
     :state-time state-time
     :items {}}))
