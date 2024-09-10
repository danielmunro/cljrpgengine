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
                          :bash 1}}})

(defn item-quantity-map
  [items]
  (apply merge (map #(hash-map (:key %) (:quantity %)) items)))

(defn remove-item!
  [state item quantity menu]
  (let [{:keys [items]} @state]
    (loop [i 0]
      (if (= item (:key (get items i)))
        (dosync
          (alter state update-in [:items i :quantity] (fn [q] (- q quantity)))
          (if (= 0 (get-in @state [:items i :quantity]))
            (do
              (alter state assoc-in [:items] (into [] (filter #(< 0 (:quantity %))) (:items @state)))
              (let [menu-index (ui/get-menu-index state menu)]
                (if (and
                      (= (get-in @state [:menus menu-index :cursor]) (count (:items @state)))
                      (> (get-in @state [:menus menu-index :cursor]) 0))
                  (alter state update-in [:menus menu-index :cursor] dec))))))
        (if (> (dec (count items)) i)
          (recur (inc i)))))))
