(ns cljrpgengine.player-test
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]
            [clojure.test :refer :all]
            [quil.core :as q]))

(deftest test-start-moving
  (testing "can start moving left"
    (let [lock (promise)]
      (q/defsketch start-moving
                   :draw (fn []
                           (let [state (state/create-state "tinytown")
                                 mob (player/get-player-first-mob state)]
                             (player/start-moving
                               state
                               :left
                               (+ (:x mob) 16)
                               (:y mob))
                             (is (contains? (:keys @state) :left))
                             (is (= (get-in @state [:player :party 0 :sprite :current-animation]) :left)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock))
  (testing "can reset moving"
    (let [lock (promise)]
      (q/defsketch reset-moving
                   :draw (fn []
                           (let [state (state/create-state "tinytown")
                                 mob (player/get-player-first-mob state)]
                             (player/start-moving
                               state
                               :right
                               (- (:x mob) 16)
                               (:y mob))
                             (player/reset-moving state :right)
                             (is (empty? (:keys @state)))
                             (is (= (get-in @state [:player :party 0 :sprite :current-animation]) :right)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock))
  )
