(ns cljrpgengine.core
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [quil.core :as q]
            [quil.middleware :as m]
            [cljrpgengine.state :as state]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]))

(defn setup
  []
  (q/frame-rate constants/target-fps)
  (q/background 0)
  (state/create-state "tinytown"))

(defn update-animations
  [state]
  (player/update-player-sprite state))

(defn update-state
  "Main loop, starting with updating animations.  Eventually, this will include
  checking for game events."
  [state]
  (update-animations state)
  (player/update-move-offsets state)
  (player/check-start-moving state)
  state)

(defn draw
  [state]
  (let [map (:map @state)
        player (:player @state)]
    (map/draw-background map)
    (sprite/draw
      (-> (:x player)
          (+ (:x-offset player)))
      (-> (:y player)
          (+ (:y-offset player)))
      (:sprite player))
    (map/draw-foreground map)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "starting game...")
  (q/defsketch hello
               :setup setup
               :size constants/window
               :update update-state
               :draw draw
               :key-pressed input/check-key-press
               :key-released input/check-key-released
               :middleware [m/fun-mode]))
