(ns cljrpgengine.initialize-game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.fight :as fight]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.shop :as shop]
            [cljrpgengine.state :as state]
            [cljrpgengine.ui :as ui]))

(defn load-room!
  [state scene room]
  (if (not= scene (:scene @state))
    (scene/load-scene state scene room))
  (mob/load-room-mobs state scene room)
  (event/load-room-events state scene room)
  (shop/load-shops state scene room)
  (fight/load-encounters! scene room)
  (fight/set-room-encounters! (get-in @state [:map :tilemap :encounters]))
  (event/fire-room-loaded-event state room))

(defn- close-ui-if-open
  [state]
  (if (ui/is-menu-open? state)
    (ui/close-menu! state)))

(defn start
  [state]
  (dosync (alter state assoc
                 :player (player/create-new-player)
                 :map (map/load-map :tinytown :main)
                 :save-name (random-uuid)
                 :money constants/starting-money
                 :items {:light-health-potion 2
                         :light-mana-potion 1
                         :practice-sword 1})
          (alter state dissoc :new-game))
  (map/init-map state)
  (load-room! state :tinytown :main)
  (close-ui-if-open state))

(defn load-save
  [state file]
  (let [new-state (state/load-save-file file)]
    (dosync (alter state merge @new-state)
            (alter state dissoc :load-game))
    (let [{:keys [scene room]} @state]
      (scene/load-scene state scene room)
      (load-room! state scene room))
    (close-ui-if-open state)))
