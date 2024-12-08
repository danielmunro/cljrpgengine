(ns cljrpgengine.mob
  (:require [cljrpgengine.animation :as animation]
            [cljrpgengine.constants :as constants]
            [flatland.ordered.set :as oset])
  (:import (com.badlogic.gdx.graphics Texture)
           (com.badlogic.gdx.scenes.scene2d Actor)))

(def portraits-dir (str constants/resources-dir "portraits/"))

(def mobs (atom nil))

(defn- get-portrait-from-mob-type
  [type]
  ; todo make more portraits, case on type
  "edwyn.png")

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
        animation (atom nil)
        add-time-delta! (fn [delta] (swap! state-time (fn [t] (+ t delta))))]
    {:actor (doto (proxy [Actor] []
                    (draw [batch _]
                      (let [animation-to-use (or @animation @direction)
                            frame (.getKeyFrame (get animations animation-to-use) @state-time true)]
                        (.draw batch
                               frame
                               (proxy-super getX)
                               (proxy-super getY)
                               (float 1)
                               (float 1.5))))
                    (act [delta]
                      (if (not (nil? @animation))
                        (if (.isAnimationFinished (get animations @animation) (+ @state-time delta))
                          (do (swap! state-time (constantly 0))
                              (swap! animation (constantly nil)))
                          (add-time-delta! delta)))))
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
     :items (atom {})
     :equipment (atom {:head nil
                       :cloak nil
                       :torso nil
                       :wrists nil
                       :legs nil
                       :feet nil
                       :accessory nil
                       :left-hand nil
                       :right-hand nil})
     :hp (atom 1)
     :max-hp (atom 1)
     :mana (atom 1)
     :max-mana (atom 1)
     :xp (atom 0)
     :level (atom 1)
     :play-animation! (fn [animation-key]
                        (swap! animation (constantly animation-key)))
     :portrait (Texture. (str portraits-dir (get-portrait-from-mob-type mob-type)))}))
