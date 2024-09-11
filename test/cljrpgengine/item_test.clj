(ns cljrpgengine.item-test
  (:require [cljrpgengine.item :as item]
            [cljrpgengine.state :as state]
            [clojure.test :refer :all]))

(deftest item
  (testing "can add item"
    (let [state (ref state/initial-state)
          item-count (count (:items @state))]
      (item/add-item! state :cotton-tunic)
      (is (= (inc item-count) (count (:items @state))))
      (is (= :cotton-tunic (get-in @state [:items item-count :key])))
      (is (= 1 (get-in @state [:items 0 :quantity])))))
  (testing "can remove item"
    (let [state (ref state/initial-state)
          item-count (count (:items @state))]
      (item/add-item! state :light-health-potion)
      (item/remove-item! state :light-health-potion)
      (is (= item-count (count (:items @state)))))))
