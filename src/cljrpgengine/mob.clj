(ns cljrpgengine.mob
  (:require [cljrpgengine.sprite :as sprite]
            [cljrpgengine.util :as util]))

(defn find-or-create
  [state name mob]
  (if (not (util/filter-first #(= name (:name %)) (:mobs @state)))
    (dosync
     (alter state update-in [:mobs] conj (mob name)))))

(defn draw-mob
  [mob offset-x offset-y]
  (let [x (:x mob)
        y (:y mob)]
    (sprite/draw (+ x offset-x) (+ y offset-y) (:sprite mob))))

(defn create-mob
  [name direction x y sprite]
  (println "creating mob " name)
  {:name name
   :direction direction
   :x x
   :y y
   :x-offset 0
   :y-offset 0
   :sprite sprite})
