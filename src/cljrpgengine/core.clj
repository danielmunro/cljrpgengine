(ns cljrpgengine.core
  (:require [cljrpgengine.sprite :as sprite]
            [quil.core :as q]
            [quil.middleware :as m]
            [cljrpgengine.state :as state]
            [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]))

(defn setup
  []
  (q/frame-rate constants/target-fps)
  (q/background 0)
  (state/create-state))

(defn get-next-frame
  [current-frame total-frames]
  (let [next-frame (inc current-frame)]
    (if (< next-frame total-frames)
      next-frame
      0)))

(defn update-animation-frame
  [state]
  (let [current-animation (get-in @state [:player :sprite :current-animation])
        animation (get-in @state [:player :sprite :animations current-animation])
        is-playing (:is-playing animation)]
    (if (and
          (= 0 (mod (q/frame-count) (:delay animation)))
          (= true is-playing))
      (dosync
        (alter
          state
          update-in
          [:player :sprite :animations current-animation :frame]
          (fn [current-frame] (get-next-frame current-frame (:frames animation)))))))
  state)

(defn update-state
  [state]
  (update-animation-frame state))

(defn draw
  [state]
  (sprite/draw (get-in @state [:player :sprite])))

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
