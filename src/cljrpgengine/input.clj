(ns cljrpgengine.input
  (:require [cljrpgengine.chest :as chest]
            [cljrpgengine.event :as event]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menus.shop.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.save :as save]
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
  [event]
  (let [key (get-key-from-key-code (.getKeyCode event))]
    (swap! keys-pressed disj key)))

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

(defn open-chest!
  "A chest was found at the player's inspect coordinates.  Make sure it hasn't
  been opened yet, open it and get the contents."
  [chest]
  (when (not (contains? @map/opened-chests (chest/chest-key chest)))
    (if-let [gold (:gold chest)]
      (swap! player/player update-in [:gold] (fn [g] (+ g gold))))
    (if-let [item (:item chest)]
      (player/add-item! (keyword item) (get chest :quantity 1)))
    (swap! map/opened-chests (fn [opened] (conj opened (chest/chest-key chest))))))

(defn action-engaged!
  "Player is attempting to engage with something.  If on a shop, the game will
  open a shop dialog.  If next to a mob, a player will open a dialog with the
  mob.  If the player is already engaged with a mob then proceed through the
  engagement, and clear the engagement if all steps are complete."
  []
  (let [{{:keys [tilewidth tileheight]} :tileset} @map/tilemap
        {:keys [direction x y]} (player/party-leader)
        [inspect-x inspect-y] (player/get-inspect-coords x y direction tilewidth tileheight)]
    (if @event/engagement
      (if (event/engagement-done?)
        (event/clear-engagement!)
        (event/inc-engagement!))
      (if-let [mob (util/filter-first #(and (= (:x %) inspect-x) (= (:y %) inspect-y)) (vals @mob/mobs))]
        (event/create-engagement! mob)
        (if-let [shop (:name (map/get-interaction-from-coords
                              #(get-in % [:tilemap :shops])
                              x y))]
          (ui/open-menu! (shop-menu/create-menu shop))
          (if-let [chest (map/get-interaction-from-coords
                          #(get-in % [:tilemap :chests])
                          inspect-x inspect-y)]
            (open-chest! chest)))))))

(defn key-pressed!
  [event]
  (let [key (get-key-from-key-code (.getKeyCode event))]
    (if (not @locked)
      (cond
        (move-menu-cursor? key :up)
        (ui/move-cursor! :up)
        (move-menu-cursor? key :down)
        (ui/move-cursor! :down)
        (move-menu-cursor? key :left)
        (ui/move-cursor! :left)
        (move-menu-cursor? key :right)
        (ui/move-cursor! :right)
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
        (save/save)
        (= key :space)
        (action-engaged!)
        (= key :m)
        (ui/open-menu! (party-menu/create-menu))
        (= key :escape)
        (System/exit 0)
        (= key :d)
        (player/play-animation! :dance))
      (cond
        (= key :escape)
        (System/exit 0)))))
