(ns cljrpgengine.util-test
  (:require [cljrpgengine.util :as util]
            [clojure.test :refer :all]))

(deftest opposite-direction
  (testing "can produce the opposite direction"
    (is (= :down (util/opposite-direction :up)))
    (is (= :up (util/opposite-direction :down)))
    (is (= :right (util/opposite-direction :left)))
    (is (= :left (util/opposite-direction :right)))))
