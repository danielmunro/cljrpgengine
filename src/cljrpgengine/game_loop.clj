(ns cljrpgengine.game-loop
  (:require [cljrpgengine.fight :as fight]
            [cljrpgengine.log :as log]
            [cljrpgengine.map :as map]
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
    (ui/dialog mob ((:messages monolog) message-index))))

(defn- draw-map
  "Draw the map layers, mobs, and player."
  [state]
  (if (contains? (:nodes @state) :map)
    (let [{scene-map :map} @state
          {:keys [x y x-offset y-offset]} @player/player
          player-mob (player/party-leader)
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
      (map/draw-background @window/graphics scene-map adjusted-x adjusted-y)
      (dorun
       (for [m (sort-by :y (vals @mob/mobs))]
         (mob/draw-mob m adjusted-x adjusted-y)))
      (mob/draw-mob (assoc player-mob
                           :x (:x @player/player)
                           :y (:y @player/player)
                           :x-offset (:x-offset @player/player)
                           :y-offset (:y-offset @player/player)
                           :direction (:direction @player/player)) adjusted-x adjusted-y)
      (map/draw-foreground @window/graphics scene-map adjusted-x adjusted-y))))

(defn- draw
  "Redraw the screen, including backgrounds, mobs, and player."
  [state]
  (if @fight/encounter
    (fight/draw)
    (draw-map state))
  (let [{:keys [engagement menus]} @state]
    (if engagement
      (draw-dialog engagement))
    (ui/draw-menus menus))
  (effect/apply-effects state))

(defn- update-animations
  "Update all animations."
  [state elapsed-nano]
  (swap! animation-update (fn [current] (+ current elapsed-nano)))
  (let [nodes (:nodes @state)]
    (if (contains? nodes :player)
      (player/update-player-sprite! elapsed-nano))
    (if (contains? nodes :mobs)
      (mob/update-mob-sprites! elapsed-nano))
    (swap! animation-update (fn [amount] (- amount constants/time-per-frame-ns)))))

(defn- change-map!
  "Transport the player to a different map and put them at the given entrance."
  [state scene room entrance]
  (log/info (format "exit triggered :: scene: %s, room: %s, entrance: %s" scene room, entrance))
  (let [new-map (map/load-map scene room)
        {:keys [x y]} (map/get-entrance new-map entrance)]
    (effect/add-fade-in state)
    (dosync
     (alter state assoc-in [:map] new-map)
     (swap! player/player assoc :x x :y y)))
  (initialize-game/load-room! state scene room))

(defn- check-exits
  "Check the player's current location for an exit."
  [state]
  (let [{:keys [map]} @state
        {:keys [x y]} @player/player]
    (if-let [exit
             (map/get-interaction-from-coords
              map
              (fn [map] (filter #(= :exit (:type %)) (get-in map [:tilemap :warps])))
              x y)]
      (change-map! state (:scene exit) (:room exit) (:to exit)))))

(defn- do-player-updates
  "Main loop player updates."
  [state time-elapsed-ns]
  (check-exits state)
  (let [is-moving? (:is-moving? @player/player)]
    (player/update-move-offset! time-elapsed-ns)
    (if (and
         is-moving?
         (not (:is-moving? @player/player)))
      (let [encounter (fight/check-encounter-collision)]
        (if (and
             encounter
             (< (rand) (:encounter-rate encounter)))
          (do
            (ui/open-menu! state (player-select-menu/create-menu state))
            (fight/start! encounter))))))
  (player/check-start-moving state))

(defn- do-mob-updates
  "Main loop mob updates."
  [state time-elapsed-ns]
  (mob/update-move-offsets! state time-elapsed-ns)
  (mob/update-mobs state))

(defn- update-state
  "Main loop."
  [state time-elapsed-ns]
  (update-animations state time-elapsed-ns)
  (if (fight/is-active?)
    (do
      (fight/update-fight time-elapsed-ns)
      (if (not (fight/is-active?))
        (ui/open-menu! state (gains-menu/create-menu state))))
    (let [nodes (:nodes @state)]
      (if (contains? nodes :player)
        (do-player-updates state time-elapsed-ns))
      (if (contains? nodes :mobs)
        (do-mob-updates state time-elapsed-ns))))
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
