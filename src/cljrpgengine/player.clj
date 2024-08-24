(ns cljrpgengine.player
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.util :as util]))

(defn create-new-player
  [x y]
  {:party [(mob/create-mob
            :fireas
            "Fireas"
            :down
            x y
            (sprite/create-from-name :fireas))]})

(defn get-player-first-mob
  [state]
  (get-in @state [:player :party 0]))

(defn start-moving
  [state key new-x new-y]
  (if
   (and
    (not
     (mob/blocked-by-mob?
      (get-in @state [:player :party 0])
      (:mobs @state)
      new-x
      new-y
      (get-in @state [:map :tileset :tilewidth])))
    (not
     (map/is-blocking?
      (get-in @state [:map :tilemap])
      (get-in @state [:map :tileset])
      new-x
      new-y)))
    (dosync (alter state update :keys conj key)
            (alter state update-in [:player :party 0 :sprite :current-animation] (constantly key))
            (alter state update-in [:player :party 0 :sprite :animations (keyword key) :is-playing] (constantly true))
            (alter state update-in [:player :party 0] assoc
                   :x-offset (- (get-in @state [:player :party 0 :x]) new-x)
                   :y-offset (- (get-in @state [:player :party 0 :y]) new-y)
                   :x new-x
                   :y new-y
                   :direction key))
    (dosync
     (alter state update-in [:player :party 0 :sprite :current-animation] (constantly key))
     (alter state update-in [:player :party 0 :direction] (constantly key)))))

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
         (= 0 (:y-offset mob))
         (= nil (:dialog @state)))
      (do
        (if (= last-key :up)
          (start-moving state :up x (- y tile-height))
          (if (= last-key :down)
            (start-moving state :down x (+ y tile-height))
            (if (= last-key :left)
              (start-moving state :left (- x tile-width) y)
              (if (= last-key :right)
                (start-moving state :right (+ x tile-width) y)))))))))

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

(defn change-map
  [state area-name room entrance-name]
  (let [new-map (map/load-map area-name room)
        entrance (map/get-entrance new-map entrance-name)]
    (dosync
     (alter state update-in [:map] (constantly new-map))
     (alter state update-in [:player :party 0] assoc
            :x (:x entrance)
            :y (:y entrance))
     (alter state update-in [:mobs] (constantly #{})))))

(defn check-exits
  [state]
  (let [mob (get-player-first-mob state)
        x-offset (:x-offset mob)
        y-offset (:y-offset mob)]
    (if (and (= 0 y-offset)
             (= 0 x-offset))
      (let [exit (map/get-exit-warp-from-coords (:map @state) (:x mob) (:y mob))]
        (if exit
          (change-map state (:scene exit) (:room exit) (:to exit)))))))

(defn action-engaged
  [state]
  (if (:dialog @state)
    (if (= (:dialog-index @state) (- (count (:dialog @state)) 1))
      (dosync (alter state dissoc :dialog :dialog-index))
      (dosync (alter state update :dialog-index inc)))
    (let [tile-size (get-in @state [:map :tileset :tilewidth])
          direction (get-in @state [:player :party 0 :direction])
          x (get-in @state [:player :party 0 :x])
          y (get-in @state [:player :party 0 :y])
          inspect-x (if (= :left direction)
                      (- x  tile-size)
                      (if (= :right direction)
                        (+ x tile-size)
                        x))
          inspect-y (if (= :up direction)
                      (- y tile-size)
                      (if (= :down direction)
                        (+ y tile-size)
                        y))
          mob (util/filter-first #(and (= (:x %) inspect-x) (= (:y %) inspect-y)) (:mobs @state))]
      (if mob
        (dosync
         (alter state assoc
                :dialog (:dialog (event/get-dialog-event state (:identifier mob)))
                :dialog-index 0))))))
