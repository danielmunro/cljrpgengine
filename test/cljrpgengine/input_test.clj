(ns cljrpgengine.input-test
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.input :as input]
            [cljrpgengine.player :as player]
            [cljrpgengine.test-util :as test-util]
            [clojure.test :refer :all]))

(def chest-gold 1)
(def chest-item-quantity 1)

(defn- create-chest
  []
  {:gold chest-gold
   :item :light-health-potion
   :quantity chest-item-quantity
   :id 1})

(deftest chest

  (testing "can open a chest and receive its contents"
    (test-util/setup-new-player)
    (let [chest (create-chest)]
      ;; when
      (input/open-chest! chest)

      ;; then
      (is (= (+ constants/starting-money chest-gold) (:gold @player/player)))
      (is (= chest-item-quantity (-> @player/player :items :light-health-potion)))))

  (testing "cannot get items from a chest twice"
    (test-util/setup-new-player)
    (let [chest (create-chest)]
      ;; given - we open the chest
      (input/open-chest! chest)

      ;; when - we try to open the chest a second time
      (input/open-chest! chest)

      ;; then - we only have gold and items from one opening
      (is (= (+ constants/starting-money chest-gold) (:gold @player/player)))
      (is (= chest-item-quantity (-> @player/player :items :light-health-potion)))))

  (testing "can get more than one item quantity"
    (test-util/setup-new-player)
    ;; given
    (let [chest (assoc (create-chest) :quantity 5)]
      ;; when
      (input/open-chest! chest)

      ;; then
      (is (= 5 (-> @player/player :items :light-health-potion))))))
