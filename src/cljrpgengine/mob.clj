(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.util :as util]
            [cljrpgengine.class :as class]
            [cljrpgengine.window :as window]
            [clojure.java.io :as io]))

(defn is-standing-still
  [mob]
  (let [{:keys [x-offset y-offset]} mob]
    (and
     (= 0 x-offset)
     (= 0 y-offset))))

(defn draw-mob
  [mob offset-x offset-y]
  (let [x (+ (:x mob) (:x-offset mob))
        y (+ (:y mob) (:y-offset mob))]
    (sprite/draw (+ x offset-x) (+ y offset-y) (:sprite mob))))

(defn create-mob
  ([identifier name class level direction x y sprite portrait]
   (println "creating mob" name)
   (let [hp (reduce + (repeatedly level #(class/hp-for-level class)))
         mana (reduce + (repeatedly level #(class/mana-for-level class)))]
     {:identifier identifier
      :name name
      :direction direction
      :x x
      :y y
      :x-offset 0
      :y-offset 0
      :sprite sprite
      :portrait (if portrait
                  {:filename portrait
                   :image (util/load-image portrait)}
                  nil)
      :class class
      :level level
      :hp hp
      :max-hp hp
      :mana mana
      :max-mana mana}))
  ([identifier name class level direction x y sprite]
   (create-mob identifier name class level direction x y sprite nil))
  ([identifier name direction x y sprite]
   (create-mob identifier name :none 0 direction x y sprite nil)))

(defn update-room-mobs
  [state mobs]
  (let [room-loaded (:room-loaded @state)
        room (get-in @state [:map :room])
        room-mobs (get mobs room)]
    (if (not= room room-loaded)
      (dosync (alter state assoc
                     :mobs room-mobs
                     :room-loaded room)))))

(defn blocked-by-mob?
  [mobs new-x new-y tile-size]
  (let [height (second constants/character-dimensions)]
    (some
     #(not= false %)
     (map
      #(and
        (util/collision-detected?
         new-x
         (-> (- height tile-size)
             (+ new-y))
         (+ new-x tile-size)
         (+ new-y tile-size)
         (% :x)
         (-> (- height tile-size)
             (+ (% :y)))
         (+ (% :x) tile-size)
         (+ (% :y) tile-size)))
      (vals mobs)))))

(defn set-destination
  [state mob coords]
  (dosync (alter state assoc-in [:mobs mob :destination] coords)))

(defn play-animation!
  [state update-path animation]
  (dosync (alter state assoc-in (conj update-path :sprite :current-animation) animation)
          (alter state assoc-in (conj update-path :sprite :animations (keyword animation) :is-playing) true)))

(defn set-position!
  [state update-path position]
  (dosync
   (alter state update-in update-path assoc
          :x (first position)
          :y (second position)
          :x-offset 0
          :y-offset 0)))

(defn set-mob-move-offsets!
  [state update-path direction x y new-x new-y]
  (dosync
   (alter state update-in update-path assoc
          :x-offset (- x new-x)
          :y-offset (- y new-y)
          :moved 0
          :x new-x
          :y new-y
          :direction direction)))

(defn start-moving!
  [state {:keys [identifier x y]} direction new-x new-y]
  (play-animation! state [:mobs identifier] direction)
  (set-mob-move-offsets! state [:mobs identifier] direction x y new-x new-y))

(defn- do-update-move-offset!
  [state update-in-path offset-prop sprite-path min-or-max amount]
  (let [update-path-moved (conj update-in-path :moved)
        update-path-frame (conj sprite-path :animations (get-in @state (conj sprite-path :current-animation)) :frame)
        update-path-offset (conj update-in-path offset-prop)
        frame-increment (/ constants/tile-size 2)]
    (dosync
     (alter state update-in update-path-offset (fn [off] (min-or-max 0 (+ off amount))))
     (alter state update-in update-path-moved (fn [moved] (+ moved (abs amount))))
     (if (<= frame-increment (get-in @state update-path-moved))
       (do
         (alter state update-in update-path-moved (fn [moved] (- moved frame-increment)))
         (alter state update-in
                update-path-frame
                (fn [frame] (sprite/get-sprite-frame (get-in @state sprite-path) frame))))))))

(defn update-move-offset!
  [state x-offset y-offset update-in-path sprite-path elapsed-nano]
  (let [amount (/ elapsed-nano constants/move-delay)]
    (cond
      (< x-offset 0)
      (do-update-move-offset! state update-in-path :x-offset sprite-path min amount)
      (< 0 x-offset)
      (do-update-move-offset! state update-in-path :x-offset sprite-path max (- amount))
      (< y-offset 0)
      (do-update-move-offset! state update-in-path :y-offset sprite-path min amount)
      (< 0 y-offset)
      (do-update-move-offset! state update-in-path :y-offset sprite-path max (- amount))))
  (let [current-animation (get-in @state (conj sprite-path :current-animation))]
    (if (and (or (not= 0 x-offset) (not= 0 y-offset))
             (is-standing-still (get-in @state update-in-path))
             (:is-playing (get-in @state (conj sprite-path :animations current-animation))))
      (dosync
       (alter state assoc-in (conj sprite-path :animations current-animation :is-playing) false)
       (alter state assoc :is-moving? false)))))

(defn update-move-offsets!
  [state elapsed-nano]
  (let [{:keys [mobs]} @state]
    (dorun
     (for [m (vals mobs)]
       (let [{:keys [x-offset y-offset]} m]
         (update-move-offset! state x-offset y-offset [:mobs (:identifier m)] [:mobs (:identifier m) :sprite] elapsed-nano))))))

(defn check-start-moving
  [state mob direction-moving]
  (let [{:keys [x y]} mob
        {{{:keys [tilewidth tileheight]} :tileset} :map} @state]
    (if (is-standing-still mob)
      (cond
        (= direction-moving :up)
        (start-moving! state mob :up x (- y tileheight))
        (= direction-moving :down)
        (start-moving! state mob :down x (+ y tileheight))
        (= direction-moving :left)
        (start-moving! state mob :left (- x tilewidth) y)
        (= direction-moving :right)
        (start-moving! state mob :right (+ x tilewidth) y)))))

(defn update-mobs
  [state]
  (let [{:keys [mobs]} @state]
    (dorun
     (for [mob (vals mobs)]
       (let [{:keys [x y destination]} mob
             to-x (first destination)
             to-y (second destination)]
         (if destination
           (cond
             (< y to-y)
             (check-start-moving state mob :down)
             (< to-y y)
             (check-start-moving state mob :up)
             (< x to-x)
             (check-start-moving state mob :right)
             (< to-x x)
             (check-start-moving state mob :left))))))))

(defn update-sprite!
  [state update-path time-elapsed-ns {:keys [sprite]}]
  (let [current-animation (:current-animation sprite)
        animation (get-in sprite [:animations current-animation])
        frame (:frame animation)
        next-frame (sprite/get-sprite-frame sprite frame)
        delay (:delay animation)
        time-elapsed-path (conj update-path :time-elapsed)]
    (if (and
         (:is-playing animation)
         (not (contains? sprite/move-animations current-animation)))
      (dosync
       (alter state update-in time-elapsed-path (fn [amount] (+ amount time-elapsed-ns)))
       (if (< delay (/ (get-in @state time-elapsed-path) 1000000))
         (do
           (alter state update-in time-elapsed-path (fn [amount] (- amount (* delay 1000000))))
           (if (and (not (contains? (:props animation) :loop))
                    (= 0 next-frame))
             (alter state update-in update-path assoc
                    :is-playing false
                    :frame 0)
             (alter state assoc-in (conj update-path :frame) next-frame))))))))

(defn update-mob-sprites!
  [state time-elapsed-ns]
  (dorun
   (for [m (-> (:mobs @state)
               -> (vals))]
     (let [{:keys [sprite identifier]} m
           {:keys [current-animation]} sprite]
       (update-sprite! state [:mobs identifier :sprite :animations current-animation] time-elapsed-ns m)))))

(defn load-room-mobs
  [state scene room]
  (let [file-path (str constants/scenes-dir (name scene) "/" (name room) "/mobs")
        dir (io/file file-path)]
    (dosync (alter state assoc :mobs {}))
    (if (.exists dir)
      (let [mob-files (.listFiles dir)]
        (dosync
         (dorun
          (for [mob-file mob-files]
            (let [mob-data (read-string (slurp (str file-path "/" (.getName mob-file))))
                  {:keys [identifier name direction coords sprite]} mob-data
                  mob (create-mob identifier
                                  name
                                  direction
                                  (first coords)
                                  (second coords)
                                  (sprite/create sprite))]
              (alter state assoc-in [:mobs identifier] mob)))))))))
