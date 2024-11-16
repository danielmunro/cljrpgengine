(ns cljrpgengine.game-loop
  (:require [cljrpgengine.fight :as fight]
            [cljrpgengine.input :as input]
            [cljrpgengine.log :as log]
            [cljrpgengine.scene :as scene]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.effect :as effect]
            [cljrpgengine.initialize-game :as initialize-game]
            [cljrpgengine.window :as window]
            [cljrpgengine.menus.fight.player-select-menu :as player-select-menu]
            [cljrpgengine.menus.fight.gains-menu :as gains-menu]))

(def animation-update (atom 0))
(def last-time (atom (System/nanoTime)))
(def timer (atom 0))
(def draws (atom 0))
(def sleep-length (atom 12))

(defn- draw-dialog
  "Draw the dialog the player is currently engaged in."
  [engagement]
  (let [{:keys [dialog dialog-index message-index]} engagement
        monolog (get dialog dialog-index)
        mob-identifier (:mob monolog)
        mob (if (= :player mob-identifier)
              (player/party-leader)
              (get @mob/mobs mob-identifier))]
    (if monolog
      (ui/dialog mob ((:messages monolog) message-index)))))

(defn- draw-map
  "Draw the map layers, mobs, and player."
  []
  (if (scene/has-node? :map)
    (let [{:keys [x y x-offset y-offset] :as leader} (player/party-leader)
          x-plus-offset (+ x x-offset)
          y-plus-offset (+ y y-offset)
          x-window-offset (-> constants/screen-width
                              (/ 2)
                              (- x-plus-offset))
          y-window-offset (-> constants/screen-height
                              (/ 2)
                              (- y-plus-offset))
          character-x (-> (first constants/character-dimensions)
                          (/ 2))
          character-y (-> (second constants/character-dimensions)
                          (/ 2))
          adjusted-x (- x-window-offset character-x)
          adjusted-y (- y-window-offset character-y)]
      (map/draw-background @window/graphics adjusted-x adjusted-y)
      (doseq [m (sort-by :y (vals @mob/mobs))]
        (mob/draw-mob m adjusted-x adjusted-y))
      (mob/draw-mob leader adjusted-x adjusted-y)
      (map/draw-foreground @window/graphics adjusted-x adjusted-y))))

(defn- draw
  "Redraw the screen, including backgrounds, mobs, and player."
  [state]
  (if @fight/encounter
    (fight/draw)
    (draw-map))
  (let [{:keys [engagement]} @state]
    (if engagement
      (draw-dialog engagement))
    (ui/draw-menus))
  (effect/apply-effects))

(defn- update-animations!
  "Update all animations."
  [elapsed-nano]
  (swap! animation-update (fn [current] (+ current elapsed-nano)))
  (if (scene/has-node? :player)
    (player/update-player-sprite! elapsed-nano))
  (if (scene/has-node? :mobs)
    (mob/update-mob-sprites! elapsed-nano))
  (swap! animation-update (fn [amount] (- amount constants/time-per-frame-ns))))

(defn- change-map!
  "Transport the player to a different map and put them at the given entrance."
  [state scene room entrance]
  (log/info (format "exit triggered :: scene: %s, room: %s, entrance: %s" scene room, entrance))
  (map/load-tilemap scene room)
  (let [{:keys [x y]} (map/get-entrance entrance)
        {:keys [identifier]} (player/party-leader)]
    (effect/add-fade-in)
    (swap! player/party update-in [identifier] assoc :x x :y y))
  (initialize-game/load-room! state scene room))

(defn- check-exits
  "Check the player's current location for an exit."
  [state]
  (let [{:keys [x y]} (player/party-leader)]
    (if-let [exit
             (map/get-interaction-from-coords
              (fn [m] (filter #(= :exit (:type %)) (get-in m [:tilemap :warps])))
              x y)]
      (change-map! state (:scene exit) (:room exit) (:to exit)))))

(defn- do-player-updates
  "Main loop player updates."
  [state time-elapsed-ns]
  (check-exits state)
  (let [{:keys [is-moving? identifier x-offset y-offset]} (player/party-leader)]
    (mob/update-move-offset! player/party identifier x-offset y-offset time-elapsed-ns)
    (if (and
         is-moving?
         (not (:is-moving? (player/party-leader))))
      (let [encounter (fight/check-encounter-collision)]
        (if (and
             encounter
             (< (rand) (:encounter-rate encounter)))
          (do
            (ui/open-menu! (player-select-menu/create-menu state))
            (fight/start! encounter))))))
  (player/check-start-moving state (last @input/keys-pressed)))

(defn- do-mob-updates
  "Main loop mob updates."
  [time-elapsed-ns]
  (mob/update-move-offsets! time-elapsed-ns)
  (mob/update-mobs))

(defn- update-state
  "Main loop."
  [state time-elapsed-ns]
  (update-animations! time-elapsed-ns)
  (if (fight/is-active?)
    (do
      (fight/update-fight time-elapsed-ns)
      (if (not (fight/is-active?))
        (ui/open-menu! (gains-menu/create-menu state))))
    (do
      (if (scene/has-node? :player)
        (do-player-updates state time-elapsed-ns))
      (if (scene/has-node? :mobs)
        (do-mob-updates time-elapsed-ns))))
  state)

(defn run-game!
  [state]
  (while true
    (Thread/sleep @sleep-length)
    (let [current-time (System/nanoTime)
          time-diff (- current-time @last-time)]
      (window/new-graphics)
      (draw state)
      (update-state state time-diff)
      (window/draw-graphics)
      (swap! timer (fn [amount] (+ amount time-diff)))
      (swap! draws inc)
      (if (< constants/nano-per-second @timer)
        (do
          (if (> @draws constants/target-fps)
            (swap! sleep-length inc)
            (swap! sleep-length dec))
          (swap! draws (fn [_] 0))
          (swap! timer (fn [amount] (- amount constants/nano-per-second)))))
      (swap! last-time (fn [_] current-time)))))
