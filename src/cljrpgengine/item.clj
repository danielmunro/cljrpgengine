(ns cljrpgengine.item
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.util :as util]))

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

(defn compare-equipment
  [to-remove to-equip]
  (into {}
        (map (fn [attribute]
               {attribute (- (get (:attributes to-equip) attribute 0)
                             (get (:attributes to-remove) attribute 0))})
             util/attribute-order)))
