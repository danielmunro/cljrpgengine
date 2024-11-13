(ns cljrpgengine.player-test
  (:require [cljrpgengine.input :as input]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.player :as player]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.test-util :as test-util]
            [clojure.test :refer :all]))

(deftest test-start-moving
  (testing "can start moving left"
    (sprite/load-sprites)
    (test-util/create-new-state)
    (let [{:keys [x y identifier]} (player/party-leader)]
      (player/start-moving!
       :left
       (+ x (get-in @map/tilemap [:tileset :tilewidth]))
       y)
      (is (= (get-in @player/party [identifier :sprite :current-animation]) :left))))
  (testing "can reset moving"
    (sprite/load-sprites)
    (test-util/create-new-state)
    (let [{:keys [x y identifier]} (player/party-leader)]
      (player/start-moving!
       :right
       (- x (get-in @map/tilemap [:tileset :tilewidth]))
       y)
      (is (= (get-in @player/party [identifier :sprite :current-animation]) :right)))))
