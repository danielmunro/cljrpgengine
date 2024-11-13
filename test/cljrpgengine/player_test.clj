(ns cljrpgengine.player-test
  (:require [cljrpgengine.tilemap :as map]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.test-util :as test-util]
            [clojure.test :refer :all]))

(deftest test-start-moving
  (testing "can start moving left"
    (sprite/load-sprites)
    (let [state (test-util/create-new-state)
          {:keys [x y identifier]} (player/party-leader)]
      (player/start-moving!
       state
       :left
       (+ x (get-in @map/tilemap [:tileset :tilewidth]))
       y)
      (is (contains? (:keys @state) :left))
      (is (= (get-in @player/party [identifier :sprite :current-animation]) :left))))
  (testing "can reset moving"
    (sprite/load-sprites)
    (let [state (test-util/create-new-state)
          {:keys [x y identifier]} (player/party-leader)]
      (player/start-moving!
       state
       :right
       (- x (get-in @map/tilemap [:tileset :tilewidth]))
       y)
      (dosync (alter state update :keys disj :right))
      (is (empty? (:keys @state)))
      (is (= (get-in @player/party [identifier :sprite :current-animation]) :right)))))
