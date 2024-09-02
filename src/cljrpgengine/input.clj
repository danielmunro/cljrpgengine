(ns cljrpgengine.input
  (:require [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menu :as menu]))

(defn key-released!
  [state {:keys [key]}]
  (dosync
   (alter state update :keys disj key))
  state)

(defn- move-menu-cursor?
  [state key-pressed key-check]
  (and
   (= key-pressed key-check)
   (ui/is-menu-open? state)))

(defn- should-quit-menu?
  [state key]
  (and
   (= key :q)
   (ui/is-menu-open? state)))

(defn- evaluate-menu-action?
  [state key]
  (and
   (= key :space)
   (ui/is-menu-open? state)))

(defn key-pressed!
  [state {:keys [key key-code]}]
  (cond
    (move-menu-cursor? state key :up)
    (ui/move-cursor! state :up)
    (move-menu-cursor? state key :down)
    (ui/move-cursor! state :down)
    (move-menu-cursor? state key :left)
    (ui/move-cursor! state :left)
    (move-menu-cursor? state key :right)
    (ui/move-cursor! state :right)
    (should-quit-menu? state key)
    (ui/close-menu! state)
    (evaluate-menu-action? state key)
    (.key-pressed (get-in @state [:menus (ui/last-menu-index state) :menu]))
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
    (player/action-engaged! state)
    (= key :m)
    (ui/open-menu! state (menu/create-party-menu state))
    (= key-code 27)
    (println "escape key pressed")
    ;(dosync
    ;  (alter state update-in [:menus] drop-last))
    )
  state)
