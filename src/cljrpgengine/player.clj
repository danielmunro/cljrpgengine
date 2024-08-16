(ns cljrpgengine.player
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.sprite :as sprite]))

(defn create-player
  [x y]
  {:x x
   :y y
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
  (if (not (map/is-blocking? (get-in @state [:map :tilemap]) (get-in @state [:map :tileset]) new-x new-y))
    (dosync (alter state update :keys conj key)
            (alter state update-in [:player :sprite :current-animation] (constantly key))
            (alter state update-in [:player :sprite :animations (keyword key) :is-playing] (constantly true))
            (alter state update-in [:player :x-offset] (constantly (- (get-in @state [:player :x]) new-x)))
            (alter state update-in [:player :y-offset] (constantly (- (get-in @state [:player :y]) new-y)))
            (alter state update-in [:player :x] (constantly new-x))
            (alter state update-in [:player :y] (constantly new-y)))))

(defn check-start-moving
  [state]
  (let [player (:player @state)
        x (:x player)
        y (:y player)
        keys (:keys @state)
        last-key (first keys)
        tilewidth (get-in @state [:map :tileset :tilewidth])
        tileheight (get-in @state [:map :tileset :tileheight])]
    (if (and
          (= 0 (:x-offset player))
          (= 0 (:y-offset player)))
      (do
        (if (= last-key :up)
          (start-moving state :up x (- y tileheight))
          (if (= last-key :down)
            (start-moving state :down x (+ y tileheight))
            (if (= last-key :left)
              (start-moving state :left (- x tilewidth) y)
              (if (= last-key :right)
                (start-moving state :right (+ x tilewidth) y))))))
      )))

(defn reset-moving
  [state key]
  (dosync
    (alter state update :keys disj key))
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
        (fn [frame] (sprite/get-sprite-frame sprite frame)))
      (if (and
            (= 0 (get-in @state [:player :x-offset]))
            (= 0 (get-in @state [:player :y-offset])))
        (alter state update-in [:player :sprite :animations current-animation :is-playing] (constantly false))))))

(defn update-move-offsets
  [state]
  (let [player (:player @state)
        x-offset (:x-offset player)
        y-offset (:y-offset player)]
    (dosync
      (if (> 0 x-offset)
        (alter state update-in [:player :x-offset] inc)
        (if (< 0 x-offset)
          (alter state update-in [:player :x-offset] dec)
          (if (> 0 y-offset)
            (alter state update-in [:player :y-offset] inc)
            (if (< 0 y-offset)
              (alter state update-in [:player :y-offset] dec))))))))
