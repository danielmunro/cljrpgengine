(ns cljrpgengine.scenes.tinytown-scene
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.state :as state]))

(def mobs (atom {}))

(defn sordna-events
  [state]
  (event/create-dialog-event!
   state
   []
   :sordna
   ["Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
    "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]
   [(event/move-mob :sordna [352 176])
    (event/grant :sordna-moved)])
  (event/create-room-loaded-event!
   state
   [(event/granted :sordna-moved)]
   :main
   [(event/set-mob-coords :sordna [352 176])]))

(defn andros-events
  [state]
  (event/create-dialog-event!
   state
   [(event/has-item :blemished-amulet)]
   :andros
   ["Oh, that amulet has true potential. Let me clean it up for you."]
   [(event/lose-item :blemished-amulet)
    (event/grant :lose-blemished-amulet)])
  (event/create-dialog-event!
   state
   [(event/granted :lose-blemished-amulet)
    (event/not-has-item :brilliant-amulet)]
   :andros
   ["Here it is. Much better!"]
   [(event/gain-item :brilliant-amulet)])
  (event/create-dialog-event!
   state
   [(event/granted :intro-andros)]
   :andros
   ["It was a pleasure to meet you."])
  (event/create-dialog-event!
   state
   []
   :andros
   ["Hello traveller. Please, take a seat. I must tell you a story."]
   [(event/grant :intro-andros)]))

(defn agnos-events
  [state]
  (event/create-dialog-event!
   state
   [(event/has-item :brilliant-amulet)]
   :agnos
   ["Oh wow! The amulet look amazing!"]
   [(event/mob-animation :agnos :dance)
    (event/player-animation :dance)])
  (event/create-dialog-event!
   state
   [(event/granted :gain-blemished-item)]
   :agnos
   ["I hope the amulet serves you well."])
  (event/create-dialog-event!
   state
   [(event/not-granted :gain-blemished-item)]
   :agnos
   ["Please, take this amulet."]
   [(event/gain-item :blemished-amulet)
    (event/grant :gain-blemished-item)]))

(defn initialize-scene
  [state]
  (sordna-events state)
  (andros-events state)
  (agnos-events state)
  (state/update-nodes state #{:player :mobs :map}))

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

(defn create
  [state]
  (TinytownScene. state))
