(ns cljrpgengine.player
  (:require [cljrpgengine.effect :as effect]
            [cljrpgengine.event :as event]
            [cljrpgengine.fight :as fight]
            [cljrpgengine.map :as map]
            [cljrpgengine.menus.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(defn create-new-player
  []
  {:party [(mob/create-mob
            :edwyn
            "Fireas"
            :warrior 1
            :down 0 0
            (sprite/create :edwyn)
            "portraits/edwyn.png"
            #{:bash})
           (mob/create-mob
            :edwyn
            "Dingus"
            :mage 1
            :down 0 0
            (sprite/create :edwyn)
            "portraits/edwyn.png"
            #{:magic-missile})
           (mob/create-mob
            :edwyn
            "Prabble"
            :rogue 1
            :down 0 0
            (sprite/create :edwyn)
            "portraits/edwyn.png"
            #{:hamstring})
           (mob/create-mob
            :edwyn
            "Floodlegor"
            :cleric 1
            :down 0 0
            (sprite/create :edwyn)
            "portraits/edwyn.png"
            #{:cure-light})]
   :x     0
   :y     0
   :x-offset 0
   :y-offset 0
   :direction :down})

(defn start-moving!
  [state direction new-x new-y]
  (let [{:keys                     [mobs]
         {:keys [x y]}             :player
         {:keys [tileset tilemap]} :map} @state]
    (if
     (and
      (not
       (mob/blocked-by-mob?
        mobs
        new-x
        new-y
        (:tilewidth tileset)))
      (not
       (map/is-blocking?
        tilemap
        tileset
        new-x
        new-y)))
      (do
        (mob/play-animation! state [:player :party 0] direction)
        (mob/set-mob-move-offsets! state [:player] direction x y new-x new-y)
        (dosync (alter state update :keys conj direction)
                (alter state assoc :is-moving? true)))
      (dosync
       (alter state assoc-in [:player :party 0 :sprite :current-animation] direction)
       (alter state assoc-in [:player :direction] direction)))))

(defn check-start-moving
  [state]
  (let [{:keys [keys engagement menus player]
         {{:keys [tilewidth tileheight]} :tileset} :map
         {:keys [x y]} :player} @state
        last-key (first keys)]
    (if (and
         (mob/is-standing-still player)
         (not engagement)
         (= 0 (count menus))
         (not @fight/encounter))
      (cond
        (= last-key :up)
        (start-moving! state :up x (- y tileheight))
        (= last-key :down)
        (start-moving! state :down x (+ y tileheight))
        (= last-key :left)
        (start-moving! state :left (- x tilewidth) y)
        (= last-key :right)
        (start-moving! state :right (+ x tilewidth) y)))))

(defn update-player-sprite!
  [state time-elapsed-ns]
  (let [{{:keys [x-offset y-offset]
          [{:keys [sprite] {:keys [current-animation]} :sprite}] :party} :player} @state]
    (mob/update-sprite!
     state
     [:player :party 0 :sprite :animations current-animation]
     time-elapsed-ns
     {:x-offset x-offset
      :y-offset y-offset
      :sprite sprite})))

(defn update-move-offset!
  [state elapsed-nano]
  (let [{{:keys [x-offset y-offset]} :player} @state]
    (mob/update-move-offset! state x-offset y-offset [:player] [:player :party 0 :sprite] elapsed-nano)))

(defn- create-engagement!
  [state mob]
  (let [identifier (:identifier mob)
        event (event/get-dialog-event state identifier)]
    (dosync (alter state assoc
                   :engagement {:dialog (:dialog event)
                                :dialog-index 0
                                :message-index 0
                                :mob identifier
                                :event event
                                :mob-direction (get-in mob [:sprite :current-animation])})
            (alter state assoc-in
                   [:mobs identifier :sprite :current-animation]
                   (util/opposite-direction (get-in @state [:player :direction]))))))

(defn- engagement-done?
  [engagement]
  (= (count (:dialog engagement)) (:dialog-index engagement)))

(defn- clear-engagement!
  [state]
  (let [{:keys [engagement]} @state
        {:keys [mob mob-direction] {:keys [outcomes]} :event} engagement
        current-animation-path [:mobs mob :sprite :current-animation]]
    (let [current-animation (get-in @state current-animation-path)]
      (event/apply-outcomes! state outcomes)
      (dosync
       (if (= current-animation (get-in @state current-animation-path))
         (alter state assoc-in current-animation-path mob-direction))
       (alter state dissoc :engagement)))))

(defn- inc-engagement!
  [state]
  (dosync (alter state update-in [:engagement :message-index] inc))
  (let [dialog-index (get-in @state [:engagement :dialog-index])]
    (if (= (count (get-in @state [:engagement :dialog dialog-index :messages]))
           (get-in @state [:engagement :message-index]))
      (do
        (dosync
         (alter state assoc-in [:engagement :message-index] 0)
         (alter state update-in [:engagement :dialog-index] inc))
        (if (engagement-done? (:engagement @state))
          (clear-engagement! state))))))

(defn- get-inspect
  [tile-position dir-1 dir-2 direction-facing tile-size]
  (if (= dir-1 direction-facing)
    (- tile-position tile-size)
    (if (= dir-2 direction-facing)
      (+ tile-position tile-size)
      tile-position)))

(defn- get-inspect-coords
  "Get the coordinates that the player is inspecting.  The inspected coords
  depends on the position and direction of the player.
      1
     2P3
      4
  Assuming P represents the player's coords, the inspected coords are 1 if
  facing up, 2 if facing left, 3 if facing right, and 4 if facing down."
  [x y direction tilewidth tileheight]
  [(get-inspect x :left :right direction tilewidth)
   (get-inspect y :up :down direction tileheight)])

(defn action-engaged!
  "Player is attempting to engage with something.  If on a shop, the game will
  open a shop dialog.  If next to a mob, a player will open a dialog with the
  mob.  If the player is already engaged with a mob then proceed through the
  engagement, and clear the engagement if all steps are complete."
  [state]
  (let [{:keys [engagement mobs map]
         {:keys [direction x y]} :player
         {{:keys [tilewidth tileheight]} :tileset} :map} @state
        [inspect-x inspect-y] (get-inspect-coords x y direction tilewidth tileheight)]
    (if engagement
      (if (engagement-done? engagement)
        (clear-engagement! state)
        (inc-engagement! state))
      (if-let [mob (util/filter-first #(and (= (:x %) inspect-x) (= (:y %) inspect-y)) (vals mobs))]
        (create-engagement! state mob)
        (if-let [shop (:name (map/get-interaction-from-coords
                              map
                              #(get-in % [:tilemap :shops])
                              x
                              y))]
          (ui/open-menu! state (shop-menu/create-menu state shop)))))))
