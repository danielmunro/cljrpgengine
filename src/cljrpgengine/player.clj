(ns cljrpgengine.player
  (:require [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.sprite :as sprite]))

(defn create-new-player
  [x y]
  {:party [(mob/create-mob
             "fireas"
             x y
             0 0
             (sprite/create-from-name :fireas))]})

(defn get-player-first-mob
  [state]
  (get-in @state [:player :party 0]))

(defn start-moving
  [state key new-x new-y]
  (if (not (map/is-blocking? (get-in @state [:map :tilemap]) (get-in @state [:map :tileset]) new-x new-y))
    (dosync (alter state update :keys conj key)
            (alter state update-in [:player :party 0 :sprite :current-animation] (constantly key))
            (alter state update-in [:player :party 0 :sprite :animations (keyword key) :is-playing] (constantly true))
            (alter state update-in [:player :party 0 :x-offset] (constantly (- (get-in @state [:player :party 0 :x]) new-x)))
            (alter state update-in [:player :party 0 :y-offset] (constantly (- (get-in @state [:player :party 0 :y]) new-y)))
            (alter state update-in [:player :party 0 :x] (constantly new-x))
            (alter state update-in [:player :party 0 :y] (constantly new-y)))))

(defn check-start-moving
  [state]
  (let [mob (get-player-first-mob state)
        x (:x mob)
        y (:y mob)
        keys (:keys @state)
        last-key (first keys)
        tile-width (get-in @state [:map :tileset :tilewidth])
        tile-height (get-in @state [:map :tileset :tileheight])]
    (if (and
          (= 0 (:x-offset mob))
          (= 0 (:y-offset mob)))
      (do
        (if (= last-key :up)
          (start-moving state :up x (- y tile-height))
          (if (= last-key :down)
            (start-moving state :down x (+ y tile-height))
            (if (= last-key :left)
              (start-moving state :left (- x tile-width) y)
              (if (= last-key :right)
                (start-moving state :right (+ x tile-width) y)))))))))

(defn reset-moving
  [state key]
  (dosync
    (alter state update :keys disj key))
  state)

(defn update-player-sprite
  [state]
  (let [mob (get-player-first-mob state)
        sprite (:sprite mob)
        current-animation (:current-animation sprite)]
    (dosync
      (alter
        state
        update-in
        [:player :party 0 :sprite :animations current-animation :frame]
        (fn [frame] (sprite/get-sprite-frame sprite frame)))
      (if (and
            (= 0 (:x-offset mob))
            (= 0 (:y-offset mob)))
        (alter state update-in [:player :party 0 :sprite :animations current-animation :is-playing] (constantly false))))))

(defn update-move-offsets
  [state]
  (let [mob (get-player-first-mob state)
        x-offset (:x-offset mob)
        y-offset (:y-offset mob)]
    (dosync
      (if (> 0 x-offset)
        (alter state update-in [:player :party 0 :x-offset] inc)
        (if (< 0 x-offset)
          (alter state update-in [:player :party 0 :x-offset] dec)
          (if (> 0 y-offset)
            (alter state update-in [:player :party 0 :y-offset] inc)
            (if (< 0 y-offset)
              (alter state update-in [:player :party 0 :y-offset] dec))))))))
