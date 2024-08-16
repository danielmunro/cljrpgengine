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
                           (let [state (state/create-state "tinytown")]
                             (player/start-moving
                               state
                               :left
                               (+ (get-in @state [:player :x]) 16)
                               (get-in @state [:player :y]))
                             (is (contains? (:keys @state) :left))
                             (is (= (get-in @state [:player :sprite :current-animation]) :left)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock))
  (testing "can reset moving"
    (let [lock (promise)]
      (q/defsketch reset-moving
                   :draw (fn []
                           (let [state (state/create-state "tinytown")]
                             (player/start-moving
                               state
                               :right
                               (- (get-in @state [:player :x]) 16)
                               (get-in @state [:player :y]))
                             (player/reset-moving state :right)
                             (is (empty? (:keys @state)))
                             (is (= (get-in @state [:player :sprite :current-animation]) :right)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock))
  )
