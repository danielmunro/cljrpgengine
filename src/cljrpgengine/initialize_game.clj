(ns cljrpgengine.initialize-game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.event :as event]
            [cljrpgengine.log :as log]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.fight :as fight]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.shop :as shop]
            [cljrpgengine.state :as state]
            [cljrpgengine.ui :as ui]))

(defn load-room!
  [state scene room]
  (log/info (format "loading room :: %s" room))
  (if (not= scene (:scene @state))
    (scene/load-scene state scene room))
  (mob/load-room-mobs scene room)
  (event/load-room-events scene room)
  (shop/load-shops state scene room)
  (fight/load-encounters! scene room)
  (fight/set-room-encounters! (get-in @map/tilemap [:tilemap :encounters]))
  (event/fire-room-loaded-event room))

(defn- close-ui-if-open
  []
  (if (ui/is-menu-open?)
    (ui/close-menu!)))

(defn- init-map
  []
  (let [{:keys [x y direction]} (map/get-warp "start")
        {:keys [identifier]} (player/party-leader)]
    (swap! player/party
           update-in
           [identifier]
           assoc
           :x x
           :y y
           :x-offset 0
           :y-offset 0
           :direction direction)))

(defn start
  [state]
  (player/create-new-player)
  (map/load-tilemap :tinytown :main)
  (dosync (alter state assoc
                 :save-name (random-uuid))
          (alter state dissoc :new-game))
  (player/add-item! :light-health-potion 2)
  (player/add-item! :light-mana-potion)
  (player/add-item! :practice-sword)
  (init-map)
  (load-room! state :tinytown :main)
  (close-ui-if-open))

(defn load-save
  [state file]
  (let [new-state (state/load-save-file file)]
    (dosync (alter state merge @new-state)
            (alter state dissoc :load-game))
    (let [{:keys [scene room]} @state]
      (scene/load-scene state scene room)
      (load-room! state scene room))
    (close-ui-if-open)))
