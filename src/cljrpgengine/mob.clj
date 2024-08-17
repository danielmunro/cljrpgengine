(ns cljrpgengine.mob)

(defn create-mob
  [name x y x-offset y-offset sprite]
  {
   :name name
   :x x
   :y y
   :x-offset x-offset
   :y-offset y-offset
   :sprite sprite
   })
