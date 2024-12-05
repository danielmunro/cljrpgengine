(ns cljrpgengine.player)

(def party (atom []))

(def items (atom {}))

(defn add-item!
  [item]
  (if-let [quantity (get @items item)]
    (swap! items assoc item (inc quantity))
    (swap! items assoc item 1)))
