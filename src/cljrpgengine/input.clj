(ns cljrpgengine.input
  (:require [cljrpgengine.menu :as menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.party-menu :as party-menu])
  (:import (java.awt.event KeyEvent)))

(defn get-key-from-key-code
  [key-code]
  (cond
    (= key-code KeyEvent/VK_UP)
    :up
    (= key-code KeyEvent/VK_DOWN)
    :down
    (= key-code KeyEvent/VK_LEFT)
    :left
    (= key-code KeyEvent/VK_RIGHT)
    :right
    (= key-code KeyEvent/VK_SPACE)
    :space
    (= key-code KeyEvent/VK_S)
    :s
    (= key-code KeyEvent/VK_M)
    :m
    (= key-code KeyEvent/VK_ESCAPE)
    :escape
    (= key-code KeyEvent/VK_Q)
    :q
    (= key-code KeyEvent/VK_D)
    :d))

(defn key-released!
  [state event]
  (let [key (get-key-from-key-code (.getKeyCode event))]
    (dosync
     (alter state update :keys disj key)))
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
    (ui/is-menu-open? state)
    (not (contains? menu/non-closeable-menus (ui/get-last-menu state)))))

(defn- evaluate-menu-action?
  [state key]
  (and
   (= key :space)
   (ui/is-menu-open? state)))

(defn key-pressed!
  [state event]
  (let [key (get-key-from-key-code (.getKeyCode event))]
    (if (not (:lock @state))
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
        (ui/open-menu! state (party-menu/create-menu state))
        (= key :escape)
        (System/exit 0)
        (= key :d)
        (mob/play-animation! state [:player :party 0] :dance))
      (cond
        (= key :escape)
        (System/exit 0))))
  state)
