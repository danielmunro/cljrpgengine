(ns cljrpgengine.input
  (:require [cljrpgengine.mob :as mob]))

(defn check-key-released
  [state {:keys [key]}]
  (mob/reset-moving state key)
  state)

(defn check-key-press
  [state {:keys [key]}]
  (if (not (contains? (get-in @state [:player :keys]) key))
    (cond
      (= key :up)
      (mob/start-moving state :up)
      (= key :down)
      (mob/start-moving state :down)
      (= key :left)
      (mob/start-moving state :left)
      (= key :right)
      (mob/start-moving state :right)))
  state)
