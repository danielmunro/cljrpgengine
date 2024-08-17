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
  ;(state/create-new-state "tinytown")
  (state/create-from-latest-save "9442e963-e8c2-4246-a7f2-a7e5eccf65d2")
  )

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
        mob (player/get-player-first-mob state)]
    (map/draw-background map)
    (sprite/draw
      (-> (:x mob)
          (+ (:x-offset mob)))
      (-> (:y mob)
          (+ (:y-offset mob)))
      (:sprite mob))
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
