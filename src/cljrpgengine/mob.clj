(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.util :as util]
            [cljrpgengine.class :as class]))

(defn no-move-offset
  [mob]
  (let [{:keys [x-offset y-offset]} mob]
    (and
     (= 0 x-offset)
     (= 0 y-offset))))

(defn draw-mob
  [g mob offset-x offset-y]
  (let [x (+ (:x mob) (:x-offset mob))
        y (+ (:y mob) (:y-offset mob))]
    (sprite/draw g (+ x offset-x) (+ y offset-y) (:sprite mob))))

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
      :portrait portrait
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
    (if (and
         (not= room room-loaded)
         room-mobs)
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

(defn start-moving!
  [state mob key new-x new-y]
  (let [identifier (:identifier mob)]
    (dosync (alter state assoc-in [:mobs identifier :sprite :current-animation] key)
            (alter state assoc-in [:mobs identifier :sprite :animations (keyword key) :is-playing] true)
            (alter state update-in [:mobs identifier] assoc
                   :x-offset (- (:x mob) new-x)
                   :y-offset (- (:y mob) new-y)
                   :x new-x
                   :y new-y
                   :direction key))))

(defn update-move-offset!
  [state x-offset y-offset update-in-path sprite-path elapsed-nano]
  (let [amount (/ elapsed-nano 20000000)]
    (cond
      (< x-offset 0)
      (dosync (alter state update-in (conj update-in-path :x-offset) (fn [off] (if (< 0 (+ off amount)) 0 (+ off amount)))))
      (< 0 x-offset)
      (dosync (alter state update-in (conj update-in-path :x-offset) (fn [off] (if (< (- off amount) 0) 0 (- off amount)))))
      (< y-offset 0)
      (dosync (alter state update-in (conj update-in-path :y-offset) (fn [off] (if (< 0 (+ off amount)) 0 (+ off amount)))))
      (< 0 y-offset)
      (dosync (alter state update-in (conj update-in-path :y-offset) (fn [off] (if (< (+ off amount) 0) 0 (- off amount)))))))
  (let [current-animation (get-in @state (conj sprite-path :current-animation))]
    (if (and (no-move-offset (get-in @state update-in-path))
             (:is-playing (get-in @state (conj sprite-path :animations current-animation))))
      (dosync (alter state update-in (conj sprite-path :animations current-animation :is-playing) (constantly false))))))

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
    (if (no-move-offset mob)
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
  [state update-path {:keys [sprite]}]
  (let [current-animation (:current-animation sprite)
        animation (get-in sprite [:animations current-animation])
        frame (:frame animation)
        next-frame (sprite/get-sprite-frame sprite frame)]
    (if (:is-playing animation)
      (dosync
        (if (and (not (:is-looping animation))
                 (= 0 next-frame))
          (alter state assoc-in update-path :is-playing false)
          (alter state update-in (conj update-path :frame) (constantly next-frame)))))))

(defn update-mob-sprites!
  [state]
  (dorun
   (for [m (-> (:mobs @state)
               -> (vals))]
     (let [{:keys [sprite identifier]} m
           {:keys [current-animation]} sprite]
       (update-sprite! state [:mobs identifier :sprite :animations current-animation] m)))))
