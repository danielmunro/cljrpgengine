(ns cljrpgengine.mob
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.util :as util]
            [cljrpgengine.class :as class]))

(defn add-if-missing!
  [state mob]
  (if (not (util/filter-first #(= (:name mob) (:name %)) (:mobs @state)))
    (dosync
     (alter state update-in [:mobs] conj mob))))

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
  [mob mobs new-x new-y tile-size]
  (let [height (constants/character-dimensions 1)
        mobs-to-search (filter #(not= (:name mob) (:name %)) (vals mobs))]
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
      mobs-to-search))))

(defn set-destination
  [state mob coords]
  (let [mobs (:mobs @state)]
    (loop [i 0]
      (if (< i (count mobs))
        (if (= mob (:identifier (get mobs i)))
          (dosync (alter state assoc-in [:mobs i :destination] coords))
          (recur (inc i)))))))

(defn update-mobs
  [state]
  (dorun
   (for [mob (:mobs @state)]
     (when-let [destination (:destination mob)]
       (println (:identifier mob))
       (println destination)))))
