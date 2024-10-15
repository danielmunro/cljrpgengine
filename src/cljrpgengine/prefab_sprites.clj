(ns cljrpgengine.prefab-sprites
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.sprite :as sprite]))

(defn- create-edwyn
  [direction]
  (sprite/create
   :edwyn
   "edwyn.png"
   (first constants/character-dimensions)
   (second constants/character-dimensions)
   3
   3
   direction
   {:down  (sprite/create-animation [0 1 0 2] nil #{:loop})
    :left  (sprite/create-animation [3 4 3 5] nil #{:loop})
    :right (sprite/create-animation [6 7 6 8] nil #{:loop})
    :up    (sprite/create-animation [9 10 9 11] nil #{:loop})
    :dance (sprite/create-animation [0 3 9 6] 4 #{})}))

(defn create-from-name
  [name direction]
  (cond
    (= :edwyn name)
    (create-edwyn direction)))
