(ns cljrpgengine.util)

(defn opposite-direction
  [direction]
  (case direction
    :up
    :down
    :down
    :up
    :left
    :right
    :right
    :left))
