(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.util :as util]
            [cljrpgengine.class :as class]
            [clojure.java.io :as io]))

(def mobs (atom nil))

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
  ([identifier name class level direction x y sprite portrait skills xp]
   (log/debug (format "creating mob %s" name))
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
      :max-mana mana
      :skills skills
      :xp xp}))
  ([identifier name class level direction x y sprite]
   (create-mob identifier name class level direction x y sprite nil #{} 0))
  ([identifier name direction x y sprite]
   (create-mob identifier name :none 0 direction x y sprite nil #{} 0)))

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

(defn set-destination!
  [identifier coords]
  (swap! mobs assoc-in [identifier :destination] coords))

(defn play-animation!
  [mobs identifier animation]
  (swap! mobs assoc-in [identifier :sprite :current-animation] animation)
  (swap! mobs assoc-in [identifier :sprite :animations (keyword animation) :is-playing] true))

(defn set-position!
  [identifier position]
  (swap! mobs update-in [identifier] assoc
         :x (first position)
         :y (second position)
         :x-offset 0
         :y-offset 0))

(defn set-mob-move-offsets!
  [mobs identifier direction x y new-x new-y]
  (swap! mobs update-in [identifier] assoc
         :x-offset (- x new-x)
         :y-offset (- y new-y)
         :moved 0
         :x new-x
         :y new-y
         :direction direction))

(defn start-moving!
  [{:keys [identifier x y]} direction new-x new-y]
  (play-animation! mobs identifier direction)
  (set-mob-move-offsets! mobs identifier direction x y new-x new-y))

(defn- do-update-move-offset!
  [mobs identifier offset-prop min-or-max amount]
  (let [frame-increment (/ constants/tile-size 2)
        current-animation (get-in @mobs [identifier :sprite :current-animation])]
    (swap! mobs update-in [identifier offset-prop] (fn [offset] (min-or-max 0 (+ offset amount))))
    (swap! mobs update-in [identifier :moved] (fn [moved] (+ moved (abs amount))))
    (if (<= frame-increment (get-in @mobs [identifier :moved]))
      (do
        (swap! mobs update-in [identifier :moved] (fn [moved] (- moved frame-increment)))
        (swap! mobs update-in [identifier :sprite :animations current-animation :frame]
               (fn [frame] (sprite/get-sprite-frame (get-in @mobs [identifier :sprite]) frame)))))))

(defn update-move-offset!
  [mobs identifier x-offset y-offset elapsed-nano]
  (let [amount (/ elapsed-nano constants/move-delay-ns)]
    (cond
      (< x-offset 0)
      (do-update-move-offset! mobs identifier :x-offset min amount)
      (< 0 x-offset)
      (do-update-move-offset! mobs identifier :x-offset max (- amount))
      (< y-offset 0)
      (do-update-move-offset! mobs identifier :y-offset min amount)
      (< 0 y-offset)
      (do-update-move-offset! mobs identifier :y-offset max (- amount))))
  (let [current-animation (get-in @mobs [identifier :sprite :current-animation])]
    (if (and (or (not= 0 x-offset) (not= 0 y-offset))
             (is-standing-still (get @mobs identifier))
             (get-in @mobs [identifier :sprite :animations current-animation :is-playing]))
      (do
        (swap! mobs assoc-in [identifier :sprite :animations current-animation :is-playing] false)
        (swap! mobs assoc-in [identifier :is-moving?] false)))))

(defn update-move-offsets!
  [elapsed-nano]
  (doseq [m (vals @mobs)]
    (let [{:keys [x-offset y-offset identifier]} m]
      (update-move-offset! mobs identifier x-offset y-offset elapsed-nano))))

(defn check-start-moving
  [mob direction-moving]
  (let [{:keys [x y]} mob
        {{:keys [tilewidth tileheight]} :tileset} @map/tilemap]
    (if (is-standing-still mob)
      (cond
        (= direction-moving :up)
        (start-moving! mob :up x (- y tileheight))
        (= direction-moving :down)
        (start-moving! mob :down x (+ y tileheight))
        (= direction-moving :left)
        (start-moving! mob :left (- x tilewidth) y)
        (= direction-moving :right)
        (start-moving! mob :right (+ x tilewidth) y)))))

(defn update-mobs
  []
  (doseq [mob (vals @mobs)]
    (let [{:keys [x y destination]} mob
          to-x (first destination)
          to-y (second destination)]
      (if destination
        (cond
          (< y to-y)
          (check-start-moving mob :down)
          (< to-y y)
          (check-start-moving mob :up)
          (< x to-x)
          (check-start-moving mob :right)
          (< to-x x)
          (check-start-moving mob :left))))))

(defn update-sprite!
  [{:keys [identifier sprite] {:keys [current-animation]} :sprite} time-elapsed-ns]
  (let [{:keys [frame delay is-playing props]} (get-in sprite [:animations current-animation])
        next-frame (sprite/get-sprite-frame sprite frame)
        update-path [identifier :sprite :animations current-animation]
        time-elapsed-path (conj update-path :time-elapsed)]
    (if (and
         is-playing
         (not (contains? sprite/move-animations current-animation)))
      (do
        (swap! mobs update-in time-elapsed-path (fn [amount] (+ amount time-elapsed-ns)))
        (if (< delay (/ (get-in @mobs time-elapsed-path) 1000000))
          (do
            (swap! mobs update-in time-elapsed-path (fn [amount] (- amount (* delay 1000000))))
            (if (and (not (contains? props :loop))
                     (= 0 next-frame))
              (swap! mobs update-in update-path assoc :is-playing false :frame 0)
              (swap! mobs update-in update-path assoc :frame next-frame))))))))

(defn update-mob-sprites!
  [time-elapsed-ns]
  (doseq [mob (vals @mobs)]
    (update-sprite! mob time-elapsed-ns)))

(defn load-room-mobs
  [scene room]
  (let [file-path (str constants/scenes-dir (name scene) "/" (name room) "/mobs")
        dir (io/file file-path)]
    (swap! mobs (constantly nil))
    (if (.exists dir)
      (let [mob-files (.listFiles dir)]
        (doseq [mob-file mob-files]
          (let [{:keys [identifier name direction coords sprite]}
                (read-string (slurp (str file-path "/" (.getName mob-file))))]
            (swap! mobs assoc
                   identifier (create-mob
                               identifier
                               name
                               direction
                               (first coords)
                               (second coords)
                               (sprite/create sprite)))))))))
