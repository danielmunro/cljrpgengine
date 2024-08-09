(ns cljrpgengine.player-test
  (:require [cljrpgengine.player :as player]
            [clojure.test :refer :all]
            [quil.core :as q]))

(deftest start-moving
  (testing "can start moving left"
    (let [lock (promise)]
      (q/defsketch start-moving
                   :draw (fn []
                           (let [player (ref (player/create-player))]
                             (player/start-moving player :left)
                             (is (contains? (:keys @player) :left))
                             (is (= (:facing @player) :left)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock))
  (testing "can stop moving"
    (let [lock (promise)]
      (q/defsketch reset-moving
                   :draw (fn []
                           (let [player (ref (player/create-player))]
                             (player/start-moving player :right)
                             (player/reset-moving player :right)
                             (is (empty? (:keys @player)))
                             (is (= (:facing @player) :right)))
                           (q/exit))
                   :on-close #(deliver lock true))
      @lock)))
