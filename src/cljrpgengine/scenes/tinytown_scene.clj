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
                              ["Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
                               "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]
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

(deftype TinytownScene [state]
  scene/Scene
  (initialize-scene [_] (initialize-scene state))
  (update-scene [_] (update-scene state))
  (scene-name [_] :tinytown)
  (shops [_] {:tinytown-item-shop [:light-health-potion
                                   :practice-sword
                                   :cotton-tunic]}))

(defn create-tinytown-scene
  [state]
  (TinytownScene. state))
