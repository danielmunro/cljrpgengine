(ns cljrpgengine.player-test
  (:require [cljrpgengine.player :as player]
            [clojure.test :refer :all]))

(defn- reset-state!
  []
  (swap! player/items (constantly {})))

(deftest item
  (testing "can add item"
    (reset-state!)
    (let [item-count (count @player/items)]
      (player/add-item! :cotton-tunic)
      (is (= (inc item-count) (count @player/items))))
    (is (contains? @player/items :cotton-tunic))
    (is (= 1 (:cotton-tunic @player/items))))
  (testing "can remove item"
    (reset-state!)
    (let [item-count (count @player/items)]
      (player/add-item! :blemished-amulet)
      (is (= (inc item-count) (count @player/items)))
      (player/remove-item! :blemished-amulet)
      (is (= item-count (count @player/items))))))
