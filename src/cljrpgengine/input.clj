(ns cljrpgengine.input
  (:require [cljrpgengine.player :as player]))

(defn check-key-released
  [state {:keys [key]}]
  (player/reset-moving state key)
  state)

(defn check-key-press
  [state {:keys [key]}]
  (if (not (contains? (:keys @state) key))
    (cond
      (= key :up)
      (player/start-moving state :up)
      (= key :down)
      (player/start-moving state :down)
      (= key :left)
      (player/start-moving state :left)
      (= key :right)
      (player/start-moving state :right)))
  state)
