(ns cljrpgengine.input
  (:require [cljrpgengine.player :as player]))

(defn check-key-released
  [state {:keys [key]}]
  (player/reset-moving state key)
  state)

(defn check-key-press
  [state {:keys [key]}]
  (cond
    (= key :up)
    (dosync (alter state update-in [:keys] conj :up))
    (= key :down)
    (dosync (alter state update-in [:keys] conj :down))
    (= key :left)
    (dosync (alter state update-in [:keys] conj :left))
    (= key :right)
    (dosync (alter state update-in [:keys] conj :right)))
  state)
