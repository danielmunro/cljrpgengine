(ns cljrpgengine.mob-test
  (:require [cljrpgengine.mob :as mob]
            [clojure.test :refer :all]))

(deftest test-mob
  (testing "can set the destination"
    (swap! mob/mobs (constantly {:foo (mob/create-mob :foo "foo" :down 0 0 nil nil)}))
    (mob/set-destination! :foo [1 1])
    (is (= [1 1] (get-in @mob/mobs [:foo :destination])))))
