(ns cljrpgengine.game-loop
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.new-game :as new-game]
            [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.effect :as effect]
            [cljrpgengine.window :as window]))

(def animation-update (atom 0))
(def last-time (atom (System/nanoTime)))
(def time-diff (atom 0))
(def timer (atom 0))
(def draws (atom 0))
(def sleep-length (atom 12))

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
      (ui/dialog ((:dialog engagement) (:dialog-index engagement))))
    (ui/draw-menus menus))
  (effect/apply-effects state))

(defn- update-animations
  "Update all animations."
  [state elapsed-nano]
  (swap! animation-update (fn [current] (+ current elapsed-nano)))
  (if (< constants/time-per-frame-nano @animation-update)
    (let [nodes (:nodes @state)]
      (if (contains? nodes :player)
        (player/update-player-sprite! state))
      (if (contains? nodes :mobs)
        (mob/update-mob-sprites! state))
      (swap! animation-update (fn [amount] (- amount constants/time-per-frame-nano))))))

(defn- check-room-change
  "Fire a room-loaded event whenever the player enters a new room."
  [state room-loaded]
  (let [current-room (get-in @state [:map :room])]
    (if (not= current-room room-loaded)
      (dorun
        (for [event (event/get-room-loaded-events state (keyword current-room))]
          (event/apply-outcomes! state (:outcomes event)))))))

(defn- update-state
  "Main loop, starting with updating animations.  Eventually, this will include
  checking for game events."
  [state elapsed-nano]
  (let [{:keys [new-game load-game room-loaded]} @state]
    (if new-game
      (new-game/start state))
    (if load-game
      (new-game/load-save state))
    (.update-scene (:scene @state))
    (check-room-change state room-loaded))
  (update-animations state elapsed-nano)
  (let [nodes (:nodes @state)]
    (if (contains? nodes :player)
      (do (player/update-move-offset! state elapsed-nano)
          (player/check-exits state)
          (player/check-start-moving state)))
    (if (contains? nodes :mobs)
      (do
        (mob/update-move-offsets! state elapsed-nano)
        (mob/update-mobs state))))
  state)

(defn run
  [state]
  (let [bs (:buffer-strategy @state)]
    (while true
      (Thread/sleep @sleep-length)
      (let [current-time (System/nanoTime)]
        (swap! time-diff (fn [_] (- current-time @last-time)))
        (window/new-graphics bs)
        (update-state state @time-diff)
        (draw state)
        (window/draw-graphics bs)
        (swap! timer (fn [amount] (+ amount @time-diff)))
        (swap! draws inc)
        (if (< constants/nano-per-second @timer)
          (do
            (if (> @draws constants/target-fps)
              (swap! sleep-length inc)
              (swap! sleep-length dec))
            (swap! draws (fn [_] 0))
            (swap! timer (fn [amount] (- amount constants/nano-per-second)))))
        (swap! last-time (fn [_] current-time))))))
