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
      (is (contains? (:items @state) :cotton-tunic))
      (is (= 1 (get-in @state [:items :cotton-tunic])))))
  (testing "can remove item"
    (let [state (ref state/initial-state)
          item-count (count (:items @state))]
      (item/add-item! state :blemished-amulet)
      (is (= (inc item-count) (count (:items @state))))
      (item/remove-item! state :blemished-amulet)
      (is (= item-count (count (:items @state)))))))
