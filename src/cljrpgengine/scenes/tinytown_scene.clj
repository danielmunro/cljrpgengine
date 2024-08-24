(ns cljrpgengine.scenes.tinytown-scene
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.sprite :as sprite]))

(def mobs (atom {}))

(defn initialize-scene
  [state]
  (swap! mobs
         (constantly {:item-shop [(mob/create-mob :gareth "Gareth" :down 240 80 (sprite/create-from-name :fireas :down))]
                      :main [(mob/create-mob :andros "Andros" :down 352 224 (sprite/create-from-name :fireas :down))
                             (mob/create-mob :sordna "Sordna" :down 544 320 (sprite/create-from-name :fireas :down))]}))
  (event/create-dialog-event! state
                              [(event/speak-to-condition :sordna)]
                              :sordna
                              ["Hola"]
                              #{})
  (event/create-dialog-event! state
                              [(event/speak-to-condition :andros)
                              (event/grants-condition #{:intro-andros})]
                              :andros
                              ["It was a pleasure to meet you."
                              "This is a test."]
                              #{})
  (event/create-dialog-event! state
                              [(event/speak-to-condition :andros)]
                              :andros
                              ["Hello traveller"
                              "Please, take a seat.  I must tell you a story."]
                              #{:intro-andros}))

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
