(ns cljrpgengine.input
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.shop.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.save :as state]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.party.party-menu :as party-menu]
            [cljrpgengine.util :as util])
  (:import (java.awt.event KeyEvent)))

(def keys-pressed (atom #{}))
(def locked (atom false))

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
    (swap! keys-pressed disj key))
  state)

(defn- move-menu-cursor?
  [key-pressed key-check]
  (and
   (= key-pressed key-check)
   (ui/is-menu-open?)))

(defn- should-quit-menu?
  [key]
  (and
   (= key :q)
   (ui/is-menu-open?)
   (not (contains? menu/non-closeable-menus (ui/get-last-menu)))))

(defn- evaluate-menu-action?
  [key]
  (and
   (= key :space)
   (ui/is-menu-open?)))

(defn action-engaged!
  "Player is attempting to engage with something.  If on a shop, the game will
  open a shop dialog.  If next to a mob, a player will open a dialog with the
  mob.  If the player is already engaged with a mob then proceed through the
  engagement, and clear the engagement if all steps are complete."
  [state]
  (let [{:keys [engagement]} @state
        {{:keys [tilewidth tileheight]} :tileset} @map/tilemap
        {:keys [direction x y]} (player/party-leader)
        [inspect-x inspect-y] (player/get-inspect-coords x y direction tilewidth tileheight)]
    (if engagement
      (if (event/engagement-done? engagement)
        (event/clear-engagement! state)
        (event/inc-engagement! state))
      (if-let [mob (util/filter-first #(and (= (:x %) inspect-x) (= (:y %) inspect-y)) (vals @mob/mobs))]
        (event/create-engagement! state mob)
        (if-let [shop (:name (map/get-interaction-from-coords
                              #(get-in % [:tilemap :shops])
                              x
                              y))]
          (ui/open-menu! (shop-menu/create-menu state shop)))))))

(defn key-pressed!
  [state event]
  (let [key (get-key-from-key-code (.getKeyCode event))]
    (if (not @locked)
      (cond
        (move-menu-cursor? key :up)
        (ui/move-cursor! state :up)
        (move-menu-cursor? key :down)
        (ui/move-cursor! state :down)
        (move-menu-cursor? key :left)
        (ui/move-cursor! state :left)
        (move-menu-cursor? key :right)
        (ui/move-cursor! state :right)
        (should-quit-menu? key)
        (ui/close-menu!)
        (evaluate-menu-action? key)
        (.key-pressed (get-in @ui/menus [(ui/last-menu-index) :menu]))
        (= key :up)
        (swap! keys-pressed conj :up)
        (= key :down)
        (swap! keys-pressed conj :down)
        (= key :left)
        (swap! keys-pressed conj :left)
        (= key :right)
        (swap! keys-pressed conj :right)
        (= key :s)
        (state/save)
        (= key :space)
        (action-engaged! state)
        (= key :m)
        (ui/open-menu! (party-menu/create-menu state))
        (= key :escape)
        (System/exit 0)
        (= key :d)
        (player/play-animation! :dance))
      (cond
        (= key :escape)
        (System/exit 0))))
  state)
