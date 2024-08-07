(ns cljrpgengine.mob)

(defn start-moving
  [state key]
  (dosync (alter state update-in [:player :keys] conj key)
          (alter state update-in [:player :facing] (constantly key))
          (alter state update-in [:player :sprite :current-animation] (constantly key))
          (alter state update-in [:player :sprite :animations (keyword key) :is-playing] (constantly true))))

(defn reset-moving
  [state key]
  (dosync
    (alter state update-in [:player :keys] disj key)
    (alter state update-in [:player :sprite :animations (keyword key) :is-playing] (constantly false))
    (let [keys (get-in @state [:player :keys])]
      (if (not (empty? keys))
        (alter state update-in [:player :facing] (constantly (last keys))))))
  state)
