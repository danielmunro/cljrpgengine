(ns cljrpgengine.event-test
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [clojure.test :refer :all]))

(defn reset-state!
  []
  (event/reset-events!)
  (swap! player/grants (constantly #{}))
  (swap! player/items (constantly {})))

(deftest event
  (testing "can give a grant"
    (reset-state!)
    (event/create-dialog-event!
     [(event/not-granted :test-outcome)]
     :test-mob
     ["this is a test"]
     [(event/grant :test-outcome)])
    (let [event (first @event/events)]
      (is (event/conditions-met? (:conditions event) :test-mob))
      (event/apply-outcomes! (:outcomes event))
      (is (contains? @player/grants :test-outcome))))

  (testing "has grant"
    (reset-state!)
    (event/create-dialog-event!
     [(event/granted :test-outcome)]
     :test-mob
     ["this is a test"])
    (let [event (first @event/events)]
      (is (false? (contains? @player/grants :test-outcome)))
      (player/add-grant! :test-outcome)
      (is (event/conditions-met? (:conditions event) :test-mob))))

  (testing "can give an item"
    (reset-state!)
    (event/create-dialog-event!
     [(event/not-has-item :blemished-amulet)]
     :test-mob
     ["this is a test"]
     [(event/gain-item :blemished-amulet)])
    (let [event (first @event/events)]
      (is (event/conditions-met? (:conditions event) :test-mob))
      (event/apply-outcomes! (:outcomes event))
      (is (contains? @player/items :blemished-amulet))))

  (testing "has item"
    (reset-state!)
    (event/create-dialog-event!
     [(event/has-item :blemished-amulet)]
     :test-mob
     ["this is a test"])
    (let [event (first @event/events)]
      (is (false? (event/conditions-met? (:conditions event) :test-mob)))
      (player/add-item! :blemished-amulet)
      (is (event/conditions-met? (:conditions event) :test-mob))))

  (testing "lose item"
    (event/reset-events!)
    (player/add-item! :blemished-amulet)
    (event/create-dialog-event!
     [(event/has-item :blemished-amulet)]
     :test-mob
     ["this is a test"]
     [(event/lose-item :blemished-amulet)])
    (let [event (first @event/events)]
      (event/apply-outcomes! (:outcomes event))
      (is (contains? @player/items :blemished-amulet))))

  (testing "can set a destination"
    (event/reset-events!)
    (swap! mob/mobs (constantly {:test-mob (mob/create-mob
                                            :test-mob
                                            "test-mob"
                                            :down
                                            0
                                            0
                                            nil
                                            :unspecified)}))
    (event/create-dialog-event!
     []
     :test-mob
     ["this is a test"]
     [(event/move-mob :test-mob [1 1])])
    (let [event (first @event/events)]
      (event/apply-outcomes! (:outcomes event))
      (is (= [1 1] @(:destination (get @mob/mobs :test-mob))))))

  (testing "can get an event"
    (event/reset-events!)
    (is (= nil (event/get-dialog-event :test-event)))
    (event/create-dialog-event!
     []
     :test-mob
     ["this is a test"]
     [])
    (is (event/get-dialog-event :test-mob))))
