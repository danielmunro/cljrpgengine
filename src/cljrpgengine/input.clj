(ns cljrpgengine.input
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.shop.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.state :as state]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.party.party-menu :as party-menu]
            [cljrpgengine.util :as util])
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

(defn action-engaged!
  "Player is attempting to engage with something.  If on a shop, the game will
  open a shop dialog.  If next to a mob, a player will open a dialog with the
  mob.  If the player is already engaged with a mob then proceed through the
  engagement, and clear the engagement if all steps are complete."
  [state]
  (let [{:keys [engagement map]
         {{:keys [tilewidth tileheight]} :tileset} :map} @state
        {:keys [direction x y]} @player/player
        [inspect-x inspect-y] (player/get-inspect-coords x y direction tilewidth tileheight)]
    (if engagement
      (if (event/engagement-done? engagement)
        (event/clear-engagement! state)
        (event/inc-engagement! state))
      (if-let [mob (util/filter-first #(and (= (:x %) inspect-x) (= (:y %) inspect-y)) (vals @mob/mobs))]
        (event/create-engagement! state mob)
        (if-let [shop (:name (map/get-interaction-from-coords
                              map
                              #(get-in % [:tilemap :shops])
                              x
                              y))]
          (ui/open-menu! state (shop-menu/create-menu state shop)))))))

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
        (action-engaged! state)
        (= key :m)
        (ui/open-menu! state (party-menu/create-menu state))
        (= key :escape)
        (System/exit 0)
        (= key :d)
        (player/play-animation! :dance))
      (cond
        (= key :escape)
        (System/exit 0))))
  state)
