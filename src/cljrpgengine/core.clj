(ns cljrpgengine.core
  (:require [cljrpgengine.sprite :as sprite]
            [quil.core :as q]
            [quil.middleware :as m]
            [cljrpgengine.player :as player]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]))

(defn setup []
  (q/frame-rate constants/target-fps)
  (q/background 0)
  (ref (player/create-player)))

(defn get-next-frame
  [current-frame total-frames]
  (let [next-frame (inc current-frame)]
    (if (< next-frame total-frames)
      next-frame
      0)))

(defn update-animation-frame
  [state]
  (let [player @state
        current-animation (get-in player [:sprite :current-animation])
        animation (get-in player [:sprite :animations current-animation])
        is-playing (:is-playing animation)]
    (if (and
          (= 0 (mod (q/frame-count) (:delay animation)))
          (= true is-playing))
      (dosync
        (alter
          state
          update-in
          [:sprite :animations current-animation :frame]
          (fn [current-frame] (get-next-frame current-frame (:frames animation)))))))
  state)

(defn update-state
  [state]
  (update-animation-frame state))

(defn draw [state]
  (sprite/draw (:sprite @state) (get-in @state [:sprite :current-animation])))


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
