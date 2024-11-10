(ns cljrpgengine.player
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.menus.shop.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(def player (atom nil))
(def party (atom nil))

(defn create-new-player
  []
  (swap! player
         (fn [_]
           {:x     0
            :y     0
            :x-offset 0
            :y-offset 0
            :direction :down}))
  (swap! party
         (fn [_]
           [(mob/create-mob
             :edwyn
             "Fireas"
             :warrior 1
             :down 0 0
             (sprite/create :edwyn)
             "portraits/edwyn.png"
             #{:bash}
             0)
            (mob/create-mob
             :edwyn
             "Dingus"
             :mage 1
             :down 0 0
             (sprite/create :edwyn)
             "portraits/edwyn.png"
             #{:magic-missile}
             0)
            (mob/create-mob
             :edwyn
             "Prabble"
             :rogue 1
             :down 0 0
             (sprite/create :edwyn)
             "portraits/edwyn.png"
             #{:hamstring}
             0)
            (mob/create-mob
             :edwyn
             "Floodlegor"
             :cleric 1
             :down 0 0
             (sprite/create :edwyn)
             "portraits/edwyn.png"
             #{:cure-light}
             0)])))

(defn play-animation!
  [animation]
  (swap! party assoc-in [0 :sprite :current-animation] animation)
  (swap! party assoc-in [0 :sprite :animations (keyword animation) :is-playing] true))

(defn start-moving!
  [state direction new-x new-y]
  (let [{:keys [mobs]
         {:keys [tileset tilemap]} :map} @state
        {:keys [x y]} @player]
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
        (play-animation! direction)
        (swap! player assoc :direction direction :x new-x :y new-y :x-offset (- x new-x) :y-offset (- y new-y) :moved 0 :is-moving? true)
        (dosync (alter state update :keys conj direction)))
      (do
        (swap! party assoc-in [0 :sprite :current-animation] direction)
        (swap! player assoc :direction direction)))))

(defn check-start-moving
  [state]
  (let [{:keys [keys engagement menus]
         {{:keys [tilewidth tileheight]} :tileset} :map} @state
        {:keys [x y]} @player
        last-key (first keys)]
    (if (and
         (mob/is-standing-still @player)
         (not engagement)
         (= 0 (count menus)))
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
  [time-elapsed-ns]
  (let [[{:keys [sprite] {:keys [current-animation]} :sprite}] @party
        animation (get-in sprite [:animations current-animation])
        {:keys [frame delay is-playing]} animation
        next-frame (sprite/get-sprite-frame sprite frame)
        time-elapsed-path [0 :sprite :animations current-animation :time-elapsed]]
    (if (and
         is-playing
         (not (contains? sprite/move-animations current-animation)))
      (do
        (swap! party update-in time-elapsed-path (fn [time-elapsed] (+ time-elapsed time-elapsed-ns)))
        (if (< delay (/ (get-in @party time-elapsed-path) constants/animation-delay-ns))
          (do
            (swap! party update-in time-elapsed-path
                   (fn [time-elapsed] (- time-elapsed (* delay constants/animation-delay-ns))))
            (if (and (not (contains? (:props animation) :loop))
                     (= 0 next-frame))
              (do
                (swap! party assoc-in [0 :sprite :animations current-animation :is-playing] false)
                (swap! party assoc-in [0 :sprite :animations current-animation :frame] 0))
              (swap! party assoc-in [0 :sprite :animations current-animation :frame] next-frame))))))))

(defn- do-update-move-offset!
  [offset-prop min-or-max amount]
  (let [frame-increment (/ constants/tile-size 2)
        current-animation (get-in @party [0 :sprite :current-animation])]
    (swap! player update offset-prop (fn [offset] (min-or-max 0 (+ offset amount))))
    (swap! player update :moved (fn [moved] (+ moved (abs amount))))
    (if (<= frame-increment (:moved @player))
      (do
        (swap! player update :moved (fn [moved] (- moved frame-increment)))
        (swap! party update-in [0 :sprite :animations current-animation :frame]
               (fn [frame] (sprite/get-sprite-frame (get-in @party [0 :sprite]) frame)))))))

(defn update-move-offset!
  [elapsed-nano]
  (let [amount (/ elapsed-nano constants/move-delay-ns)
        {:keys [x-offset y-offset]} @player
        current-animation (get-in @party [0 :sprite :current-animation])
        is-playing (get-in @party [0 :sprite :animations current-animation :is-playing])]
    (cond
      (< x-offset 0)
      (do-update-move-offset! :x-offset min amount)
      (< 0 x-offset)
      (do-update-move-offset! :x-offset max (- amount))
      (< y-offset 0)
      (do-update-move-offset! :y-offset min amount)
      (< 0 y-offset)
      (do-update-move-offset! :y-offset max (- amount)))
    (if (and (or (not= 0 x-offset) (not= 0 y-offset))
             (mob/is-standing-still @player)
             is-playing)
      (do
        (swap! party assoc-in [0 :sprite :animations current-animation :is-playing] false)
        (swap! player assoc :is-moving? false)))))

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
                   (util/opposite-direction (:direction @player))))))

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
         {{:keys [tilewidth tileheight]} :tileset} :map} @state
        {:keys [direction x y]} @player
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
