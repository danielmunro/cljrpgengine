(ns cljrpgengine.menu-test
  (:require [cljrpgengine.menu :as menu]
            [clojure.test :refer :all]))

(deftest text-fixed-width
  (testing "truncates lines that are too long"
    (is (= "the great broads... " (#'menu/text-fixed-width "the great broadsword of devestating cleaving" 20))))
  (testing "adds spaces for lines that are too short"
    (is (= "a potion  " (#'menu/text-fixed-width "a potion" 10))))
  (testing "does not modify a line of perfect length"
    (is (= "a potion" (#'menu/text-fixed-width "a potion" 8)))))
