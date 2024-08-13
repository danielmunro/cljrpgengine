(ns cljrpgengine.input
  (:require [cljrpgengine.player :as player]))

(defn check-key-released
  [state {:keys [key]}]
  (player/reset-moving state key)
  state)

(defn check-key-press
  [state {:keys [key]}]
  (let [player (:player @state)]
    (if (and
          (= 0 (:x-offset player))
          (= 0 (:y-offset player)))
          (cond
            (= key :up)
            (player/start-moving state :up (:x player) (dec (:y player)))
            (= key :down)
            (player/start-moving state :down (:x player) (inc (:y player)))
            (= key :left)
            (player/start-moving state :left (dec (:x player)) (:y player))
            (= key :right)
            (player/start-moving state :right (inc (:x player)) (:y player))))
      state))
