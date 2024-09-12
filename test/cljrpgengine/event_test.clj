(ns cljrpgengine.event-test
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.state :as state]
            [clojure.test :refer :all]))

(deftest event
  (testing "can give a grant"
    (let [state (ref state/initial-state)
          mob (mob/create-mob :test-mob "test-mob" :down 0 0 nil)]
      (event/create-dialog-event!
        state
        [(event/not-granted :test-outcome)]
        :test-mob
        ["this is a test"]
        [(event/grant :test-outcome)])
      (let [event (first (:events @state))]
        (is (= false (event/conditions-met state (:conditions event) mob)))
        (event/apply-outcomes! state (:outcomes event))
        (is (contains? (:grants @state) :test-outcome)))))
  (testing "can give an item"
    (let [state (ref state/initial-state)
          mob (mob/create-mob :test-mob "test-mob" :down 0 0 nil)]
      (event/create-dialog-event!
        state
        [(event/not-has-item :blemished-amulet)]
        :test-mob
        ["this is a test"]
        [(event/gain-item :blemished-amulet)])
      (let [event (first (:events @state))]
        (is (= false (event/conditions-met state (:conditions event) mob)))
        (event/apply-outcomes! state (:outcomes event))
        (is (= :blemished-amulet (:key (last (:items @state)))))))))
