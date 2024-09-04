(ns cljrpgengine.item)

(def items {:light-health-potion {:name "a potion of light health"
                                  :type :consumable
                                  :affect :restore-hp
                                  :amount 20
                                  :worth 5}
            :practice-sword {:name "a wooden practice sword"
                             :type :equipment
                             :position :weapon
                             :material :wood
                             :worth 10
                             :attributes {:slash 1}}
            :cotton-tunic {:name "a cotton tunic"
                           :type :equipment
                           :position :torso
                           :material :cotton
                           :worth 12
                           :attributes {:pierce 1
                                        :slash 1
                                        :bash 1}}})

(defn item-quantity-map
  [items]
  (apply merge (map #(hash-map (:name %) (:quantity %)) items)))
