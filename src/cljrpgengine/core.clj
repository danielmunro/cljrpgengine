(ns cljrpgengine.core
  (:require [quil.core :as q])
  (:require [cljrpgengine.draw :as draw])
  (:require [cljrpgengine.sprite :as sprite]
            [quil.middleware :as m]))

(defn setup []
  (q/frame-rate 60)
  (q/background 0)
  (ref {:player {:status :idle
                 :sprite (sprite/create
                          :fireas
                          "fireas.png"
                          16
                          24
                          {:down {:frames 4
                                  :delay 8
                                  :y-offset 0}
                           :left {:frames 4
                                  :delay 8
                                  :y-offset 1}
                           :right {:frames 4
                                   :delay 8
                                   :y-offset 2}
                           :up {:frames 4
                                :delay 8
                                :y-offset 3}
                           :sleep {:frames 1
                                   :delay 0
                                   :y-offset 4}}
                          :down)}}))

(defn get-next-frame
  [current-frame total-frames]
  (let [next-frame (inc current-frame)]
    (if (< next-frame total-frames)
      next-frame
      0)))

(defn update-animation-frame
  [state]
  (let [player (:player @state)
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
          [:player :sprite :animations current-animation :frame]
          (fn [current-frame] (get-next-frame current-frame (:frames animation))))))))

(defn start-moving
  [state direction]
  (dosync (alter state update-in [:player :status] (fn [_] :moving))
          (alter state update-in [:player :sprite :current-animation] (fn [_] direction))
          (alter state update-in [:player :sprite :animations (keyword direction) :is-playing] (fn [_] true))))

(defn reset-moving
  [state]
  (let [current-animation (get-in @state [:player :sprite :current-animation])]
    (dosync
      (alter state update-in [:player :status] (fn [_] :idle))
      (alter state update-in [:player :sprite :animations current-animation :is-playing] (fn [_] false)))))

(defn check-key-press
  [state]
  (let [key (q/key-as-keyword)]
    (dosync (alter state assoc :key key))
    (cond
      (= key :up)
      (start-moving state :up)
      (= key :down)
      (start-moving state :down)
      (= key :left)
      (start-moving state :left)
      (= key :right)
      (start-moving state :right))))

(defn update-state
  [state]
  (if (q/key-pressed?)
    (check-key-press state)
    (reset-moving state))
  (update-animation-frame state)
  state)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "starting game...")
  (q/defsketch hello
               :setup setup
               :size [300 300]
               :update update-state
               :draw draw/draw
               :middleware [m/fun-mode]))
