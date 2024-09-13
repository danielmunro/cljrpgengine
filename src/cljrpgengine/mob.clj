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
  (let [room (get-in @state [:map :room])]
    (if (contains? mobs room)
      (dorun (map #(add-if-missing! state %) (mobs room))))))

(defn blocked-by-mob?
  [mob mobs new-x new-y tile-size]
  (let [height (constants/character-dimensions 1)
        mobs-to-search (filter #(not= (:name mob) (:name %)) mobs)]
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

(defn find-mob
  [state mob]
  (util/filter-first #(= (:identifier %) mob) (:mobs @state)))
