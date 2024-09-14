(ns cljrpgengine.item
  (:require [cljrpgengine.ui :as ui]))

(def items {:light-health-potion
            {:name "a potion of light health"
             :description "Heals 10 hit points."
             :type :consumable
             :affect :restore-hp
             :amount 10
             :worth 5}
            :light-mana-potion
            {:name "a potion of light mana"
             :description "Heals 20 mana."
             :type :consumable
             :affect :restore-mana
             :amount 20
             :worth 8}
            :practice-sword
            {:name "a wooden practice sword"
             :description "A worn wooden practice sword."
             :type :equipment
             :position :weapon
             :material :wood
             :worth 10
             :attributes {:slash 1}}
            :cotton-tunic
            {:name "a cotton tunic"
             :description "A warm cotton tunic."
             :type :equipment
             :position :torso
             :material :cotton
             :worth 12
             :attributes {:pierce 1
                          :slash 1
                          :bash 1}}
            :blemished-amulet
            {:name "a blemished amulet"
             :description "An amulet which has seen better days."
             :type :other}
            :brilliant-amulet
            {:name "a brilliant amulet"
             :description "A flawless, glistening amulet."
             :type :other}})

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
