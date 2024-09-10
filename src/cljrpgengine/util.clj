(ns cljrpgengine.util)

(defn filter-first
  [f data]
  (first (filter f data)))

(defn get-index-of
  [f data]
  (first (filter-first f (map-indexed vector data))))

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
