(ns cljrpgengine.player
  (:require [cljrpgengine.sprite :as sprite]))

(defn create-player
  []
  {:x 0
   :y 0
   :x-offset 0
   :y-offset 0
   :move-amount 0
   :sprite (sprite/create
             :fireas
             "fireas.png"
             16
             24
             :down
             {:down  {:frames   4
                      :delay    8
                      :y-offset 0}
              :left  {:frames   4
                      :delay    8
                      :y-offset 1}
              :right {:frames   4
                      :delay    8
                      :y-offset 2}
              :up    {:frames   4
                      :delay    8
                      :y-offset 3}
              :sleep {:frames   1
                      :delay    0
                      :y-offset 4}})})

(defn start-moving
  [state key new-x new-y]
  (dosync (alter state update :keys conj key)
          (alter state update-in [:player :sprite :current-animation] (constantly key))
          (alter state update-in [:player :sprite :animations (keyword key) :is-playing] (constantly true))
          (alter state update-in [:player :x-offset] (constantly (* (- new-x (get-in @state [:player :x])) (get-in @state [:map :tileset :tilewidth]))))
          (alter state update-in [:player :y-offset] (constantly (* (- new-y (get-in @state [:player :y])) (get-in @state [:map :tileset :tileheight]))))
          (alter state update-in [:player :x] (constantly new-x))
          (alter state update-in [:player :y] (constantly new-y))))

(defn reset-moving
  [state key]
  (dosync
    (alter state update :keys disj key)
    (alter state update-in [:player :sprite :animations (keyword key) :is-playing] (constantly false))
    (let [keys (:keys @state)]
      (if (not (empty? keys))
        (alter state update-in [:player :sprite :current-animation] (constantly (last keys))))))
  state)

(defn update-player-sprite
  [state]
  (let [sprite (get-in @state [:player :sprite])
        current-animation (:current-animation sprite)]
    (dosync
      (alter
        state
        update-in
        [:player :sprite :animations current-animation :frame]
        (fn [frame] (sprite/get-sprite-frame sprite frame))))))
