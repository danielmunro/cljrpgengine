(ns cljrpgengine.item
  (:require [cljrpgengine.constants :as constants]))

(def items (atom {}))

(defn load-items!
  []
  (let [data (read-string (slurp (str constants/resources-dir "items.edn")))]
    (swap! items
           (constantly
            (into {} (map (fn [[key item]]
                            {key (merge item {:identifier key})}))
                  data)))))

(defn create-inventory-item
  ([item-key quantity]
   {item-key quantity})
  ([item-key]
   (create-inventory-item item-key 1)))
