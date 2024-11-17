(ns cljrpgengine.initialize-game
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.log :as log]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.fight :as fight]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.shop :as shop]
            [cljrpgengine.save :as save]
            [cljrpgengine.ui :as ui]))

(defn load-room!
  ([scene room]
   (log/info (format "loading scene room :: %s, %s" scene room))
   (if (not= scene (:scene @scene/scene))
     (scene/load-scene! scene room))
   (mob/load-room-mobs scene room)
   (event/load-room-events! scene room)
   (shop/load-shops scene room)
   (fight/load-encounters! scene room)
   (fight/set-room-encounters! (get-in @map/tilemap [:tilemap :encounters]))
   (event/fire-room-loaded-event room))
  ([]
   (let [{scene-name :name room :room} @scene/scene]
     (load-room! scene-name room))))

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
  []
  (player/create-new-player)
  (map/load-tilemap! :tinytown :main)
  (save/set-new-current-save-name)
  (player/add-item! :light-health-potion 2)
  (player/add-item! :light-mana-potion)
  (player/add-item! :practice-sword)
  (init-map)
  (load-room! :tinytown :main)
  (close-ui-if-open))

(defn load-save
  [file]
  (let [{:keys [scene room] :as data} (save/load-save-file file)]
    (save/load-player! data)
    (map/load-tilemap! scene room)
    (scene/load-scene! scene room))
  (load-room!)
  (close-ui-if-open))
