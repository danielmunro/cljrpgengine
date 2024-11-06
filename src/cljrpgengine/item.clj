(ns cljrpgengine.item
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.ui :as ui]))

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

(defn remove-item!
  ([state item-key quantity menu]
   (let [menu-index (if menu (ui/get-menu-index state menu))]
     (dosync
      (alter state update-in [:items item-key] #(- % quantity))
      (if (= 0 (get-in @state [:items item-key]))
        (do
          (alter state update-in [:items] dissoc item-key)
          (let [{{{:keys [cursor]} menu-index} :menus} @state]
            (if (and
                 menu
                 (= cursor (count (:items @state)))
                 (> cursor 0))
              (alter state update-in [:menus menu-index :cursor] dec))))))))
  ([state item-key]
   (remove-item! state item-key 1 nil)))

(defn add-item!
  [state item-key]
  (let [items (:items @state)]
    (loop [i 0]
      (if (< i (count items))
        (if (= (:key (get items i)) item-key)
          (dosync (alter state update-in [:items i :quantity] inc))
          (recur (inc i)))
        (dosync (alter state update :items conj (create-inventory-item item-key)))))))

(defn get-item-at-inventory-index
  [items index]
  (nth (keys items) index))
