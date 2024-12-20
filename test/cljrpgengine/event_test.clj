(ns cljrpgengine.event-test
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [clojure.test :refer :all]))

(deftest event

  (testing "can give a grant"
    (event/reset-events!)
    (sprite/load-sprites)
    (player/create-new-player)
    (event/create-dialog-event!
     [(event/not-granted :test-outcome)]
     :test-mob
     ["this is a test"]
     [(event/grant :test-outcome)])
    (let [event (first @event/events)]
      (is (event/conditions-met? (:conditions event) :test-mob))
      (event/apply-outcomes! (:outcomes event))
      (is (contains? (:grants @player/player) :test-outcome))))

  (testing "has grant"
    (event/reset-events!)
    (sprite/load-sprites)
    (player/create-new-player)
    (event/create-dialog-event!
     [(event/granted :test-outcome)]
     :test-mob
     ["this is a test"])
    (let [event (first @event/events)]
      (is (false? (contains? (:grants @player/player) :test-outcome)))
      (player/add-grant! :test-outcome)
      (is (event/conditions-met? (:conditions event) :test-mob))))

  (testing "can give an item"
    (event/reset-events!)
    (sprite/load-sprites)
    (player/create-new-player)
    (event/create-dialog-event!
     [(event/not-has-item :blemished-amulet)]
     :test-mob
     ["this is a test"]
     [(event/gain-item :blemished-amulet)])
    (let [event (first @event/events)]
      (is (event/conditions-met? (:conditions event) :test-mob))
      (event/apply-outcomes! (:outcomes event))
      (is (contains? (:items @player/player) :blemished-amulet))))

  (testing "has item"
    (event/reset-events!)
    (sprite/load-sprites)
    (player/create-new-player)
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
      (is (contains? (:items @player/player) :blemished-amulet))))

  (testing "can set a destination"
    (event/reset-events!)
    (swap! mob/mobs (constantly {:test-mob (mob/create-mob :test-mob "test-mob" :down 0 0 nil nil)}))
    (event/create-dialog-event!
     []
     :test-mob
     ["this is a test"]
     [(event/move-mob :test-mob [1 1])])
    (let [event (first @event/events)]
      (event/apply-outcomes! (:outcomes event))
      (is (= [1 1] (get-in @mob/mobs [:test-mob :destination])))))

  (testing "can get an event"
    (event/reset-events!)
    (is (= nil (event/get-dialog-event :test-event)))
    (event/create-dialog-event!
     []
     :test-mob
     ["this is a test"]
     [])
    (is (event/get-dialog-event :test-mob))))
