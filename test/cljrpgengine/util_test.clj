(ns cljrpgengine.util-test
  (:require [cljrpgengine.util :as util]
            [clojure.test :refer :all]))

(deftest test-filter-first
  (testing "filter first returns the first matching item"
    (is (= (util/filter-first #(= % 1) [0 2 3 1])))))
