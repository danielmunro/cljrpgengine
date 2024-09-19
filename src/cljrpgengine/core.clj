(ns cljrpgengine.core
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.create-scene :as create-scene]
            [cljrpgengine.ui :as ui]
            [quil.core :as q]
            [quil.middleware :as m]
            [cljrpgengine.state :as state]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]))

(def save-file (atom nil))

(defn setup
  "Setup function for the game."
  []
  (ui/init!)
  (q/frame-rate constants/target-fps)
  (q/text-font (q/create-font constants/font-family constants/text-size))
  (let [state (if @save-file
                (state/create-from-latest-save @save-file)
                (state/create-new-state (player/create-new-player) (map/load-render-map "tinytown" "main")))
        scene (create-scene/create state (:scene @state))]
    (dosync (alter state assoc :scene scene))
    (.initialize-scene scene)
    state))

(defn update-animations
  "Update all animations -- just the player right now."
  [state]
  (player/update-player-sprite! state)
  (mob/update-mob-sprites! state))

(defn update-state
  "Main loop, starting with updating animations.  Eventually, this will include
  checking for game events."
  [state]
  (.update-scene (:scene @state))
  (update-animations state)
  (player/update-move-offsets! state)
  (player/check-exits state)
  (player/check-start-moving state)
  (mob/update-move-offsets! state)
  (mob/update-mobs state)
  state)

(defn draw
  "Redraw the screen, including backgrounds, mobs, and player."
  [state]
  (let [{scene-map :map
         :keys [engagement mobs]
         {[player] :party} :player} @state
        {:keys [x y x-offset y-offset]} player
        x-plus-offset (+ x x-offset)
        y-plus-offset (+ y y-offset)
        x-window-offset (-> (first constants/window)
                            (/ 2)
                            (- x-plus-offset))
        y-window-offset (-> (second constants/window)
                            (/ 2)
                            (- y-plus-offset))
        character-x (-> (first constants/character-dimensions)
                        (/ 2))
        character-y (-> (second constants/character-dimensions)
                        (/ 2))
        adjusted-x (- x-window-offset character-x)
        adjusted-y (- y-window-offset character-y)]
    (q/background 0)
    (map/draw-background scene-map adjusted-x adjusted-y)
    (dorun
     (for [m (sort-by :y (vals mobs))]
       (mob/draw-mob m adjusted-x adjusted-y)))
    (mob/draw-mob player adjusted-x adjusted-y)
    (map/draw-foreground scene-map adjusted-x adjusted-y)
    (if engagement
      (ui/dialog ((:dialog engagement) (:dialog-index engagement))))
    (ui/draw-menus state)))

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
    :key-pressed input/key-pressed!
    :key-released input/key-released!
    :middleware [m/fun-mode]
    :features [:exit-on-close
               :keep-on-top]))
