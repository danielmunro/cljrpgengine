(ns cljrpgengine.player-test
  (:require [cljrpgengine.input :as input]
            [cljrpgengine.player :as player]
            [cljrpgengine.test-util :as test-util]
            [clojure.test :refer :all]))

(deftest test-start-moving
  (testing "can start moving left"
    (let [state (test-util/create-new-state)
          {{[mob] :party} :player} @state]
      (player/start-moving!
       state
       :left
       (+ (:x mob) 16)
       (:y mob))
      (is (contains? (:keys @state) :left))
      (is (= (get-in @state [:player :party 0 :sprite :current-animation]) :left))))
  (testing "can reset moving"
    (let [state (test-util/create-new-state)
          {{[mob] :party} :player} @state]
      (player/start-moving!
       state
       :right
       (- (:x mob) 16)
       (:y mob))
      (input/key-released! state {:key :right})
      (is (empty? (:keys @state)))
      (is (= (get-in @state [:player :party 0 :sprite :current-animation]) :right)))))
