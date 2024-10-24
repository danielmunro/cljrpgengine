(ns cljrpgengine.util
  (:require [cljrpgengine.constants :as constants])
  (:import (java.io File)
           (javax.imageio IIOException ImageIO)))

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
  "Load an image, path is relative to 'resources/'"
  [path]
  (try
    (-> (str constants/resources-dir path)
        (File.)
        (ImageIO/read))
    (catch IIOException e
      (println "could not find path: " path)
      (throw e))))
