(ns cljrpgengine.scenes.tinytown-scene
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.sprite :as sprite]))

(defn initialize-scene
  [_ state]
  (dosync
   (let [map (map/load-map "tinytown" "main")
         start (map/get-warp map "start")]
     (alter state update-in [:map] (constantly map))
     (alter state update-in [:player :party 0] assoc
            :x (:x start)
            :y (:y start)))))

(defn update-item-shop
  [state]
  (mob/find-or-create
   state
   "shop-owner"
   (fn [name] (mob/create-mob name :down 240 80 (sprite/create-from-name :fireas)))))

(defn update-scene
  [_ state]
  (cond (= "item-shop" (get-in @state [:map :room]))
        (update-item-shop state)))

(deftype TinytownScene
         []
  scene/Scene
  (initialize-scene [scene state] (initialize-scene scene state))
  (update-scene [scene state] (update-scene scene state)))

(defn create-tinytown-scene
  []
  (TinytownScene.))
