(ns cljrpgengine.util)

(defn filter-first
  [f data]
  (first (filter f data)))

(defn collision-detected?
  [ax1 ay1 ax2 ay2 bx1 by1 bx2 by2]
  (and (< ax1 bx2)
       (> ax2 bx1)
       (< ay1 by2)
       (> ay2 by1)))
