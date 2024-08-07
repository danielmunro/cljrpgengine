(ns cljrpgengine.core
  (:require [quil.core :as q])
  (:require [cljrpgengine.draw :as draw])
  (:require [cljrpgengine.sprite :as sprite]
            [quil.middleware :as m]))

(def move-keys {:up :down :left :right})

(defn setup []
  (q/frame-rate 60)
  (q/background 0)
  (ref {:player {:keys #{}
                 :facing :down
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
                                   :y-offset 4}})}}))

(defn get-next-frame
  [current-frame total-frames]
  (let [next-frame (inc current-frame)]
    (if (< next-frame total-frames)
      next-frame
      0)))

(defn update-animation-frame
  [state]
  (let [player (:player @state)
        current-animation (:facing player)
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
  [state key]
  (dosync (alter state update-in [:player :keys] conj key)
          (alter state update-in [:player :facing] (constantly key))
          (alter state update-in [:player :sprite :current-animation] (constantly key))
          (alter state update-in [:player :sprite :animations (keyword key) :is-playing] (constantly true))))

(defn reset-moving
  [state key]
  (dosync
    (alter state update-in [:player :keys] disj key)
    (alter state update-in [:player :sprite :animations (keyword key) :is-playing] (constantly false))
    (let [keys (get-in @state [:player :keys])]
      (if (not (empty? keys))
        (alter state update-in [:player :facing] (constantly (last keys))))))
  state)

(defn check-key-released
  [state {:keys [key]}]
  (reset-moving state key)
  state)

(defn check-key-press
  [state {:keys [key]}]
  (if (not (contains? (get-in @state [:player :keys]) key))
    (cond
      (= key :up)
      (start-moving state :up)
      (= key :down)
      (start-moving state :down)
      (= key :left)
      (start-moving state :left)
      (= key :right)
      (start-moving state :right)))
  state)

(defn update-state
  [state]
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
               :key-pressed check-key-press
               :key-released check-key-released
               :middleware [m/fun-mode]))
