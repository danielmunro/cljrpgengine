(ns cljrpgengine.initialize-game
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.event :as event]
            [cljrpgengine.log :as log]
            [cljrpgengine.tilemap :as tilemap]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.fight :as fight]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.shop :as shop]
            [cljrpgengine.save :as save]
            [cljrpgengine.ui :as ui]
            [clojure.java.io :as io]))

(defn- load-init-file
  []
  (log/info "loading init config")
  (let [file-path (str constants/resources-dir "init.edn")
        file (io/file file-path)]
    (if (.exists file)
      (let [data (read-string (slurp file))]
        {:scene (keyword (:scene data))
         :room (keyword (:room data))})
      (throw (ex-info "init config missing" {:file-path file-path})))))

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

(defn- set-player-to-map-start
  []
  (let [{:keys [x y direction]} (map/get-warp "start")
        {:keys [identifier]} (player/party-leader)
        direction-key (keyword direction)]
    (swap! player/party
           update-in
           [identifier]
           assoc
           :x x
           :y y
           :x-offset 0
           :y-offset 0
           :direction direction-key)
    (swap! player/party
           assoc-in
           [identifier :sprite :current-animation]
           direction-key)))

(defn start
  []
  (player/create-new-player)
  (save/set-new-current-save-name)
  (player/add-item! :light-health-potion 2)
  (player/add-item! :light-mana-potion)
  (player/add-item! :practice-sword)
  (let [{:keys [scene room]} (load-init-file)]
    (map/load-tilemap! scene room)
    (set-player-to-map-start)
    (load-room! scene room))
  (close-ui-if-open))

(defn load-save!
  [file]
  (let [{:keys [scene room opened-chests] :as data} (save/load-save-file file)]
    (save/load-player! data)
    (map/load-tilemap! scene room)
    (scene/load-scene! scene room)
    (swap! tilemap/opened-chests (constantly opened-chests)))
  (load-room!)
  (close-ui-if-open))
