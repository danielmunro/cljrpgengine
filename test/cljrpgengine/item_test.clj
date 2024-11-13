(ns cljrpgengine.item-test
  (:require [cljrpgengine.player :as player]
            [clojure.test :refer :all]))

(deftest item
  (testing "can add item"
    (player/create-new-player)
    (let [item-count (count (:items @player/player))]

      (player/add-item! :cotton-tunic)
      (is (= (inc item-count) (count (:items @player/player))))
      (is (contains? (:items @player/player) :cotton-tunic))
      (is (= 1 (get-in @player/player [:items :cotton-tunic])))))
  (testing "can remove item"
    (player/create-new-player)
    (let [item-count (count (:items @player/player))]
      (player/add-item! :blemished-amulet)
      (is (= (inc item-count) (count (:items @player/player))))
      (player/remove-item! :blemished-amulet)
      (is (= item-count (count (:items @player/player)))))))
