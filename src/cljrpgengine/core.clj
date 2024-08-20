(ns cljrpgengine.core
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [quil.core :as q]
            [quil.middleware :as m]
            [cljrpgengine.state :as state]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]))

(def save-file (atom nil))

(defn setup
  "Setup function for the game."
  []
  (q/frame-rate constants/target-fps)
  (if @save-file
    (state/create-from-latest-save @save-file)
    (state/create-new-state)))

(defn update-animations
  "Update all animations -- just the player right now."
  [state]
  (player/update-player-sprite state))

(defn update-state
  "Main loop, starting with updating animations.  Eventually, this will include
  checking for game events."
  [state]
  (update-animations state)
  (player/update-move-offsets state)
  (player/check-exits state)
  (player/check-start-moving state)
  state)

(defn draw
  "Redraw the screen, including backgrounds, mobs, and player."
  [state]
  (let [map (:map @state)
        mob (player/get-player-first-mob state)
        x (-> (:x mob)
              (+ (:x-offset mob)))
        y (-> (:y mob)
              (+ (:y-offset mob)))
        offset-x (-> (constants/window 0)
                     (/ 2)
                     (- x))
        offset-y (-> (constants/window 1)
                     (/ 2)
                     (- y))
        character-x (-> (constants/character-dimensions 0)
                        (/ 2))
        character-y (-> (constants/character-dimensions 1)
                        (/ 2))]
    (q/background 0)
    (map/draw-background map (- offset-x character-x) (- offset-y character-y))
    (sprite/draw
     (-> (+ x offset-x)
         (- character-x))
     (-> (+ y offset-y)
         (- character-y))
     (:sprite mob))
    (map/draw-foreground map (- offset-x character-x) (- offset-y character-y))))

(defn -main
  "Start the game."
  [& args]
  (if (seq args)
    (doseq [arg args]
      (if (= "-s" arg)
        (swap! save-file (constantly (first (next args)))))))
  (println "starting game...")
  (q/defsketch game
    :title constants/title
    :setup setup
    :size constants/window
    :update update-state
    :draw draw
    :key-pressed input/check-key-press
    :key-released input/check-key-released
    :middleware [m/fun-mode]
    :features [:exit-on-close
               :keep-on-top]))
