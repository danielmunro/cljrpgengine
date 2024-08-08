(ns cljrpgengine.player
  (:require [cljrpgengine.sprite :as sprite]))

(defn create-player
  []
  {:keys #{}
   :facing :down
   :sprite (sprite/create
             :fireas
             "fireas.png"
             16
             24
             {:down  {:frames   4
                      :delay    8
                      :y-offset 0}
              :left  {:frames   4
                      :delay    8
                      :y-offset 1}
              :right {:frames   4
                      :delay    8
                      :y-offset 2}
              :up    {:frames   4
                      :delay    8
                      :y-offset 3}
              :sleep {:frames   1
                      :delay    0
                      :y-offset 4}})})

(defn start-moving
  [state key]
  (dosync (alter state update :keys conj key)
          (alter state update :facing (constantly key))
          (alter state update-in [:sprite :current-animation] (constantly key))
          (alter state update-in [:sprite :animations (keyword key) :is-playing] (constantly true))))

(defn reset-moving
  [state key]
  (dosync
    (alter state update :keys disj key)
    (alter state update-in [:sprite :animations (keyword key) :is-playing] (constantly false))
    (let [keys (:keys @state)]
      (if (not (empty? keys))
        (alter state update :facing (constantly (last keys))))))
  state)
