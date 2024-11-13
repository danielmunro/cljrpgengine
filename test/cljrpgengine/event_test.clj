(ns cljrpgengine.event-test
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.state :as state]
            [clojure.test :refer :all]))

(deftest event
  (testing "can give a grant"
    (sprite/load-sprites)
    (player/create-new-player)
    (let [state (ref state/initial-state)]
      (event/create-dialog-event!
       state
       [(event/not-granted :test-outcome)]
       :test-mob
       ["this is a test"]
       [(event/grant :test-outcome)])
      (let [event (first (:events @state))]
        (is (event/conditions-met (:conditions event) :test-mob))
        (event/apply-outcomes! (:outcomes event))
        (is (contains? (:grants @player/player) :test-outcome)))))
  (testing "has grant"
    (sprite/load-sprites)
    (player/create-new-player)
    (let [state (ref state/initial-state)]
      (event/create-dialog-event!
       state
       [(event/granted :test-outcome)]
       :test-mob
       ["this is a test"])
      (let [event (first (:events @state))]
        (is (false? (contains? (:grants @player/player) :test-outcome)))
        (player/add-grant! :test-outcome)
        (is (event/conditions-met (:conditions event) :test-mob)))))
  (testing "can give an item"
    (sprite/load-sprites)
    (player/create-new-player)
    (let [state (ref state/initial-state)]
      (event/create-dialog-event!
       state
       [(event/not-has-item :blemished-amulet)]
       :test-mob
       ["this is a test"]
       [(event/gain-item :blemished-amulet)])
      (let [event (first (:events @state))]
        (is (event/conditions-met (:conditions event) :test-mob))
        (event/apply-outcomes! (:outcomes event))
        (is (contains? (:items @player/player) :blemished-amulet)))))
  (testing "has item"
    (sprite/load-sprites)
    (player/create-new-player)
    (let [state (ref state/initial-state)]
      (event/create-dialog-event!
       state
       [(event/has-item :blemished-amulet)]
       :test-mob
       ["this is a test"])
      (let [event (first (:events @state))]
        (is (false? (event/conditions-met (:conditions event) :test-mob)))
        (player/add-item! :blemished-amulet)
        (is (event/conditions-met (:conditions event) :test-mob)))))
  (testing "lose item"
    (let [state (ref state/initial-state)]
      (player/add-item! :blemished-amulet)
      (event/create-dialog-event!
       state
       [(event/has-item :blemished-amulet)]
       :test-mob
       ["this is a test"]
       [(event/lose-item :blemished-amulet)])
      (let [event (first (:events @state))]
        (event/apply-outcomes! (:outcomes event))
        (is (false? (contains? (:items @state) :blemished-amulet))))))
  (testing "can set a destination"
    (let [state (ref state/initial-state)]
      (swap! mob/mobs (constantly {:test-mob (mob/create-mob :test-mob "test-mob" :down 0 0 nil)}))
      (event/create-dialog-event!
       state
       []
       :test-mob
       ["this is a test"]
       [(event/move-mob :test-mob [1 1])])
      (let [event (first (:events @state))]
        (event/apply-outcomes! (:outcomes event))
        (is (= [1 1] (get-in @mob/mobs [:test-mob :destination]))))))
  (testing "can get an event"
    (let [state (ref state/initial-state)]
      (is (= nil (event/get-dialog-event state :test-event)))
      (event/create-dialog-event!
       state
       []
       :test-mob
       ["this is a test"]
       [])
      (is (event/get-dialog-event state :test-mob)))))
