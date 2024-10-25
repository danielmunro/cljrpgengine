(ns cljrpgengine.game-loop
  (:require [cljrpgengine.log :as log]
            [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.effect :as effect]
            [cljrpgengine.initialize-game :as initialize-game]
            [cljrpgengine.window :as window]))

(def animation-update (atom 0))
(def last-time (atom (System/nanoTime)))
(def timer (atom 0))
(def draws (atom 0))
(def sleep-length (atom 12))

(defn- draw-dialog
  [state engagement]
  (let [{:keys [dialog dialog-index message-index]} engagement
        monolog (get dialog dialog-index)
        mob-identifier (:mob monolog)
        mob (if (= :player mob-identifier)
              (get-in @state [:player :party 0])
              (get-in @state [:mobs mob-identifier]))]
    (ui/dialog mob ((:messages monolog) message-index))))

(defn- draw
  "Redraw the screen, including backgrounds, mobs, and player."
  [state]
  (if (contains? (:nodes @state) :map)
    (let [{scene-map :map
           :keys [mobs player]
           {[player-mob] :party} :player} @state
          {:keys [x y x-offset y-offset]} player
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
       (for [m (sort-by :y (vals mobs))]
         (mob/draw-mob @window/graphics m adjusted-x adjusted-y)))
      (mob/draw-mob @window/graphics (assoc player-mob
                                            :x (:x player)
                                            :y (:y player)
                                            :x-offset (:x-offset player)
                                            :y-offset (:y-offset player)
                                            :direction (:direction player)) adjusted-x adjusted-y)
      (map/draw-foreground @window/graphics scene-map adjusted-x adjusted-y)))
  (let [{:keys [engagement menus]} @state]
    (if engagement
      (draw-dialog state engagement))
    (ui/draw-menus menus))
  (effect/apply-effects state))

(defn- update-animations
  "Update all animations."
  [state elapsed-nano]
  (swap! animation-update (fn [current] (+ current elapsed-nano)))
  (let [nodes (:nodes @state)]
    (if (contains? nodes :player)
      (player/update-player-sprite! state elapsed-nano))
    (if (contains? nodes :mobs)
      (mob/update-mob-sprites! state elapsed-nano))
    (swap! animation-update (fn [amount] (- amount constants/time-per-frame-nano)))))

(defn- change-map!
  "Transport the player to a different map and put them at the given entrance."
  [state area room entrance]
  (log/info (format "exit triggered :: area: %s, room: %s, entrance: %s" area room, entrance))
  (let [new-map (map/load-map area room)
        {:keys [x y]} (map/get-entrance new-map entrance)]
    (effect/add-fade-in state)
    (dosync
     (alter state assoc-in [:map] new-map)
     (alter state update-in [:player] assoc
            :x x
            :y y)))
  (.update-scene (:scene @state))
  (initialize-game/load-room! state area room))

(defn- check-exits
  "Check the player's current location for an exit."
  [state]
  (let [{:keys [map player]} @state
        {:keys [x y]} player]
    (if (mob/is-standing-still player)
      (if-let [exit
               (map/get-interaction-from-coords
                map
                (fn [map] (filter #(= :exit (:type %)) (get-in map [:tilemap :warps])))
                x y)]
        (change-map! state (:scene exit) (:room exit) (:to exit))))))

(defn- do-player-updates
  "Main loop player updates."
  [state time-elapsed-ns]
  (check-exits state)
  (player/update-move-offset! state time-elapsed-ns)
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
  (let [nodes (:nodes @state)]
    (if (contains? nodes :player)
      (do-player-updates state time-elapsed-ns))
    (if (contains? nodes :mobs)
      (do-mob-updates state time-elapsed-ns)))
  state)

(defn run
  [state]
  (while true
    (Thread/sleep @sleep-length)
    (let [current-time (System/nanoTime)
          time-diff (- current-time @last-time)]
      (window/new-graphics)
      (update-state state time-diff)
      (draw state)
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
