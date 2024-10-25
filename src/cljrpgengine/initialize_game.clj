(ns cljrpgengine.initialize-game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.create-scene :as create-scene]
            [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.shop :as shop]
            [cljrpgengine.state :as state]
            [cljrpgengine.ui :as ui]))

(defn- init-scene
  [scene]
  (.initialize-scene scene)
  (.update-scene scene))

(defn load-room!
  [state area room]
  (mob/load-room-mobs state area room)
  (event/load-room-events state area room)
  (shop/load-shops state area room)
  (event/fire-room-loaded-event state room))

(defn- close-ui-if-open
  [state]
  (if (ui/is-menu-open? state)
    (ui/close-menu! state)))

(defn start
  [state]
  (let [scene (create-scene/create state :tinytown)]
    (dosync (alter state assoc
                   :player (player/create-new-player)
                   :map (map/load-map "tinytown" "main")
                   :save-name (random-uuid)
                   :money constants/starting-money
                   :items {:light-health-potion 2
                           :light-mana-potion 1
                           :practice-sword 1}
                   :scene scene)
            (alter state dissoc :new-game))
    (init-scene scene))
  (map/init-map state)
  (load-room! state "tinytown" "main")
  (close-ui-if-open state))

(defn load-save
  [state file]
  (let [new-state (state/load-save-file file)
        scene (create-scene/create state (:scene @new-state))]
    (dosync (alter state merge @new-state)
            (alter state dissoc :load-game)
            (alter state assoc :scene scene))
    (init-scene scene)
    (let [{{:keys [name room]} :map} @state]
      (load-room! state name room))
    (close-ui-if-open state)))
