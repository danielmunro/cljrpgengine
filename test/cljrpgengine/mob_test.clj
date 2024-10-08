(ns cljrpgengine.mob-test
  (:require [cljrpgengine.mob :as mob]
            [clojure.test :refer :all]))

(deftest test-mob
  (testing "find-or-create doesn't create more than one mob with the same name"
    (let [state (ref {:map {:room :foo}
                      :mobs []})
          mobs (atom {:foo {:foo (mob/create-mob :foo "foo" :down 0 0 nil)}})]
      (mob/update-room-mobs state @mobs)
      (mob/update-room-mobs state @mobs)
      (is (= (:mobs @state) (:foo @mobs)))))
  (testing "can set the destination"
    (let [state (ref {:mobs {:foo (mob/create-mob :foo "foo" :down 0 0 nil)}})]
      (mob/set-destination state :foo [1 1])
      (is (= [1 1] (get-in @state [:mobs :foo :destination]))))))
