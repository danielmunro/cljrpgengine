(ns cljrpgengine.item)

(def items {:light-health-potion
            {:name "a potion of light health"
             :description "Heals 20 hit points."
             :type :consumable
             :affect :restore-hp
             :amount 20
             :worth 5}
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

(defn item-type
  [key]
  (get-in items [key :type]))

(defn item-name
  [key]
  (get-in items [key :name]))

(defn is-consumable?
  [key]
  (= :consumable (item-type key)))
