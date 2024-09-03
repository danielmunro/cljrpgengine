(ns cljrpgengine.util-test
  (:require [cljrpgengine.util :as util]
            [clojure.test :refer :all]))

(deftest test-filter-first
  (testing "filter first returns the first matching item"
    (is (= (util/filter-first #(= % 1) [0 2 3 1])))))

(deftest opposite-direction
  (testing "can produce the opposite direction"
    (is (= :down (util/opposite-direction :up)))
    (is (= :up (util/opposite-direction :down)))
    (is (= :right (util/opposite-direction :left)))
    (is (= :left (util/opposite-direction :right)))))

(deftest collision-detected?
  (testing "can detect a collision"
    (is (util/collision-detected?
          0 0 10 10
          9 9 11 11)))
  (testing "can detect no collision"
    (is (not (util/collision-detected?
          0 0 10 10
          11 11 12 12)))))
