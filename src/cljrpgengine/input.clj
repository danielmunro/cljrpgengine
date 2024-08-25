(ns cljrpgengine.input
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]))

(defn key-released!
  [state {:keys [key]}]
  (dosync
   (alter state update :keys disj key))
  state)

(defn key-pressed!
  [state {:keys [key]}]
  (cond
    (= key :up)
    (dosync (alter state update-in [:keys] conj :up))
    (= key :down)
    (dosync (alter state update-in [:keys] conj :down))
    (= key :left)
    (dosync (alter state update-in [:keys] conj :left))
    (= key :right)
    (dosync (alter state update-in [:keys] conj :right))
    (= key :s)
    (state/save state)
    (= key :space)
    (player/action-engaged state)
    (= key :m)
    (dosync (alter state update-in [:menus] conj :party-menu))
    (= key :exit)
    (dosync (alter state update-in [:menus] drop-last)))
  state)
