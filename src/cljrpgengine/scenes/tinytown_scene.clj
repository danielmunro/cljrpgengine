(ns cljrpgengine.scenes.tinytown-scene
  (:require [cljrpgengine.mob :as mob]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.sprite :as sprite]))

(def mobs (atom {}))

(defn initialize-scene
  [_]
  (swap! mobs
         (constantly {:item-shop [(mob/create-mob "Gareth" :down 240 80 (sprite/create-from-name :fireas))]
                      :main [(mob/create-mob "Andros" :down 352 224 (sprite/create-from-name :fireas))
                             (mob/create-mob "Sordna" :down 544 320 (sprite/create-from-name :fireas))]})))

(defn update-scene
  [state]
  (mob/update-room-mobs state @mobs))

(deftype TinytownScene []
  scene/Scene
  (initialize-scene [scene state] (initialize-scene state))
  (update-scene [scene state] (update-scene state)))

(defn create-tinytown-scene
  []
  (TinytownScene.))
