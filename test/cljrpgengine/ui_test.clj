(ns cljrpgengine.ui-test
  (:require [cljrpgengine.ui :as ui]
            [clojure.test :refer :all]))

(deftest text-fixed-width
  (testing "truncates lines that are too long"
    (is (= "the great broads... " (ui/text-fixed-width "the great broadsword of devestating cleaving" 20))))
  (testing "adds spaces for lines that are too short"
    (is (= "a potion  " (ui/text-fixed-width "a potion" 10))))
  (testing "does not modify a line of perfect length"
    (is (= "a potion" (ui/text-fixed-width "a potion" 8)))))
