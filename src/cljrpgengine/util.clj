(ns cljrpgengine.util
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [clojure.math :as math])
  (:import (java.io File)
           (javax.imageio IIOException ImageIO)))

(def player-atb-gauge (atom nil))

(defn filter-first
  [f data]
  (first (filter f data)))

(defn collision-detected?
  [ax1 ay1 ax2 ay2 bx1 by1 bx2 by2]
  (and (< ax1 bx2)
       (> ax2 bx1)
       (< ay1 by2)
       (> ay2 by1)))

(defn opposite-direction
  [direction]
  (cond
    (= :left direction)
    :right
    (= :right direction)
    :left
    (= :up direction)
    :down
    (= :down direction)
    :up))

(defn restore-amount
  [modifier amount max-amount]
  (min modifier (- max-amount amount)))

(defn load-image
  "Load an image."
  [path]
  (try
    (-> (str path)
        (File.)
        (ImageIO/read))
    (catch IIOException e
      (log/error (format "could not find path :: %s" path))
      (throw e))))

(defn is-party-member-atb-full?
  [i]
  (= constants/atb-width (get @player-atb-gauge i)))

(defn get-xp-to-level
  [xp]
  (loop [i 0
         remaining-xp xp]
    (let [xp-for-level (math/round (* 100 (* i (/ i 2))))
          xp-minus-level (- remaining-xp xp-for-level)]
      (if (< xp-minus-level 0)
        (- xp-for-level remaining-xp)
        (recur (inc i) xp-minus-level)))))
