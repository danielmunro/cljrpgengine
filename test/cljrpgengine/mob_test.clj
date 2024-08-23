(ns cljrpgengine.mob-test
  (:require [cljrpgengine.mob :as mob]
            [clojure.test :refer :all]))

(deftest test-mob
  (testing "find-or-create doesn't create more than one mob with the same name"
    (let [state (ref {:map {:room :foo}
                      :mobs []})
          mobs (atom {:foo [(mob/create-mob "foo" :down 0 0 nil)]})]
      (mob/update-room-mobs state @mobs)
      (mob/update-room-mobs state @mobs)
      (is (= (@state :mobs) (@mobs :foo))))))
