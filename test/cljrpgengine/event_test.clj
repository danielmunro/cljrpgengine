(ns cljrpgengine.event-test
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.item :as item]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.state :as state]
            [cljrpgengine.util :as util]
            [clojure.test :refer :all]))

(defn test-mob
  []
  (mob/create-mob :test-mob "test-mob" :down 0 0 nil))

(deftest event
  (testing "can give a grant"
    (let [state (ref state/initial-state)
          mob (test-mob)]
      (event/create-dialog-event!
        state
        [(event/not-granted :test-outcome)]
        :test-mob
        ["this is a test"]
        [(event/grant :test-outcome)])
      (let [event (first (:events @state))]
        (is (false? (event/conditions-met state (:conditions event) mob)))
        (event/apply-outcomes! state (:outcomes event))
        (is (contains? (:grants @state) :test-outcome)))))
  (testing "has grant"
    (let [state (ref state/initial-state)]
      (event/create-dialog-event!
        state
        [(event/granted :test-outcome)]
        :test-mob
        ["this is a test"])
      (let [event (first (:events @state))]
        (is (false? (contains? (:grants @state) :test-outcome)))
        (dosync (alter state update :grants conj :test-outcome))
        (is (event/conditions-met state (:conditions event) :test-mob)))))
  (testing "can give an item"
    (let [state (ref state/initial-state)
          mob (test-mob)]
      (event/create-dialog-event!
        state
        [(event/not-has-item :blemished-amulet)]
        :test-mob
        ["this is a test"]
        [(event/gain-item :blemished-amulet)])
      (let [event (first (:events @state))]
        (is (false? (event/conditions-met state (:conditions event) mob)))
        (event/apply-outcomes! state (:outcomes event))
        (is (= :blemished-amulet (:key (last (:items @state))))))))
  (testing "has item"
    (let [state (ref state/initial-state)
          mob (test-mob)]
      (event/create-dialog-event!
        state
        [(event/has-item :blemished-amulet)]
        :test-mob
        ["this is a test"])
      (let [event (first (:events @state))]
        (is (false? (event/conditions-met state (:conditions event) :test-mob)))
        (item/add-item! state :blemished-amulet)
        (is (event/conditions-met state (:conditions event) :test-mob)))))
  (testing "lose item"
    (let [state (ref state/initial-state)]
      (item/add-item! state :blemished-amulet)
      (event/create-dialog-event!
        state
        [(event/has-item :blemished-amulet)]
        :test-mob
        ["this is a test"]
        [(event/lose-item :blemished-amulet)])
      (let [event (first (:events @state))]
        (event/apply-outcomes! state (:outcomes event))
        (is (false? (contains? (mapv #(:key %) (:items @state)) :blemished-amulet)))))))
