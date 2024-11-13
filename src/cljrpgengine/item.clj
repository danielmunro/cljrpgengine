(ns cljrpgengine.item
  (:require [cljrpgengine.constants :as constants]))

(def items (atom {}))

(defn load-items!
  []
  (let [data (read-string (slurp (str constants/resources-dir "items.edn")))]
    (swap! items
           (fn [_]
             data))))

(defn create-inventory-item
  ([item-key quantity]
   {item-key quantity})
  ([item-key]
   (create-inventory-item item-key 1)))

(defn item-quantity-map
  [items]
  (apply merge (map #(hash-map (:key %) (:quantity %)) items)))

(defn get-item-at-inventory-index
  [items index]
  (nth (keys items) index))
