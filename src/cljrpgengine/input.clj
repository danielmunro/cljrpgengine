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

(defn key-pressed!
  [state {:keys [key key-code]}]
  (cond
    (and
     (= key :up)
     (ui/is-menu-open? state))
    (ui/move-cursor! state :up)
    (and
     (= key :down)
     (ui/is-menu-open? state))
    (ui/move-cursor! state :down)
    (and
      (= key :q)
      (ui/is-menu-open? state))
    (ui/close-menu! state)
    (and
      (= key :space)
      (ui/is-menu-open? state))
    (.key-pressed (get-in @state [:menus (- (count (:menus @state)) 1) :menu]) state)
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
    (ui/open-menu! state (menu/create-party-menu))
    (= key-code 27)
    (println "escape key pressed")
    ;(dosync
    ;  (alter state update-in [:menus] drop-last))
    )
  state)
