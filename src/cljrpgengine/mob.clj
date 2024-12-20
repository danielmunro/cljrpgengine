(ns cljrpgengine.mob
  (:require [cljrpgengine.animation :as animation]
            [cljrpgengine.class :as class]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
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
  ([identifier name starting-direction x y mob-type class]
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
         destination (atom nil)
         add-time-delta! (fn [delta] (swap! state-time (fn [t] (+ t delta))))
         attributes (atom {:str 10
                           :int 10
                           :wis 10
                           :dex 10
                           :con 10
                           :cha 10
                           :hp (+ 10 (class/hp-for-level class))
                           :mana (+ 4 (class/mana-for-level class))
                           :hit 1
                           :dam 1
                           :ac-slash 0
                           :ac-bash 0
                           :ac-pierce 0
                           :ac-magic 0})
         equipment (atom {:head nil
                          :cloak nil
                          :torso nil
                          :wrists nil
                          :legs nil
                          :feet nil
                          :accessory nil
                          :left-hand nil
                          :right-hand nil})]
     {:window          (doto (proxy [Actor] []
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
      :identifier      identifier
      :name            name
      :key-down!       key-down!
      :key-up!         key-up!
      :direction       direction
      :keys-down       keys-down
      :add-time-delta! add-time-delta!
      :state-time      state-time
      :items           (atom {})
      :equipment       equipment
      :attributes      attributes
      :hp              (atom (:hp @attributes))
      :mana            (atom (:mana @attributes))
      :xp              (atom 0)
      :level           (atom 1)
      :play-animation! (fn [animation-key]
                         (swap! animation (constantly animation-key)))
      :portrait        (if mob-type (Texture. (str portraits-dir (get-portrait-from-mob-type mob-type))))
      :calc-attr       (fn [attr]
                         (reduce +
                                 (get @attributes attr)
                                 (map (fn [[_ equipment]]
                                        (get (:attributes (get @item/items equipment)) attr 0))
                                      @equipment)))
      :set-destination (fn [coordinates]
                         (swap! destination (constantly coordinates)))
      :destination destination
      :moving (atom false)}))
  ([identifier name starting-direction x y mob-type]
   (create-mob identifier name starting-direction x y mob-type :unspecified)))

(defn calc-attr
  [mob attr]
  (reduce +
          (get @(:attributes mob) attr)
          (map (fn [[_ equipment]] (get (:attributes (get @item/items equipment)) attr 0)) @(:equipment mob))))
