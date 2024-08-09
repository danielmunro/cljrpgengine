(ns cljrpgengine.player-test
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]
            [clojure.test :refer :all]
            [quil.core :as q]))

(deftest start-moving
  (testing "can start moving left"
    (let [lock (promise)]
      (q/defsketch start-moving
                   :draw (fn []
                           (let [state (state/create-state)]
                             (player/start-moving state :left)
                             (is (contains? (:keys @state) :left))
                             (is (= (get-in @state [:player :sprite :current-animation]) :left)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock))
  (testing "can stop moving"
    (let [lock (promise)]
      (q/defsketch reset-moving
                   :draw (fn []
                           (let [state (state/create-state)]
                             (player/start-moving state :right)
                             (player/reset-moving state :right)
                             (is (empty? (:keys @state)))
                             (is (= (get-in @state [:player :sprite :current-animation]) :right)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock))
  )
