(ns cljrpgengine.player-test
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.test-util :as test-util]
            [clojure.test :refer :all]))

(deftest test-start-moving
  (testing "can start moving left"
    (sprite/load-sprites)
    (let [state (test-util/create-new-state)
          {{{:keys [tilewidth]} :tileset} :map} @state
          {:keys [x y]} @player/player]
      (player/start-moving!
       state
       :left
       (+ x tilewidth)
       y)
      (is (contains? (:keys @state) :left))
      (is (= (get-in @player/party [0 :sprite :current-animation]) :left))))
  (testing "can reset moving"
    (sprite/load-sprites)
    (let [state (test-util/create-new-state)
          {{{:keys [tilewidth]} :tileset} :map} @state
          {:keys [x y]} @player/player]
      (player/start-moving!
       state
       :right
       (- x tilewidth)
       y)
      (dosync (alter state update :keys disj :right))
      (is (empty? (:keys @state)))
      (is (= (get-in @player/party [0 :sprite :current-animation]) :right)))))
