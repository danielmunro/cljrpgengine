(ns cljrpgengine.player
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.tilemap :as map]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]))

(def player (atom nil))
(def party (atom nil))

(defn create-new-player
  []
  (swap! player
         (fn [_]
           {:items {}
            :grants #{}
            :gold constants/starting-money}))
  (swap! party
         (fn [_]
           {:fireas
            (mob/create-mob
             :fireas
             "Fireas"
             :warrior 1
             :down 0 0
             (sprite/create :edwyn)
             "edwyn.png"
             #{:bash}
             0)
            :faedrim
            (mob/create-mob
             :faedrim
             "Dingus"
             :mage 1
             :down 0 0
             (sprite/create :edwyn)
             "edwyn.png"
             #{:magic-missile}
             0)
            :dudelgor
            (mob/create-mob
             :dudelgor
             "Dudelgor"
             :rogue 1
             :down 0 0
             (sprite/create :edwyn)
             "edwyn.png"
             #{:hamstring}
             0)
            :parthinir
            (mob/create-mob
             :parthinir
             "Parthinir"
             :cleric 1
             :down 0 0
             (sprite/create :edwyn)
             "edwyn.png"
             #{:cure-light}
             0)})))

(defn party-leader
  []
  (val (first @party)))

(defn play-animation!
  [animation]
  (let [{:keys [identifier]} (party-leader)]
    (swap! party assoc-in [identifier :sprite :current-animation] animation)
    (swap! party assoc-in [identifier :sprite :animations (keyword animation) :is-playing] true)))

(defn start-moving!
  [direction new-x new-y]
  (let [{:keys [tileset tilemap]} @map/tilemap
        {:keys [x y identifier]} (party-leader)]
    (if
     (and
      (not
       (mob/blocked-by-mob?
        @mob/mobs
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
        (swap! party update-in [identifier] assoc :direction direction :x new-x :y new-y :x-offset (- x new-x) :y-offset (- y new-y) :moved 0 :is-moving? true))
      (do
        (swap! party assoc-in [identifier :sprite :current-animation] direction)
        (swap! party assoc-in [identifier :direction] direction)))))

(defn check-start-moving
  [last-key engagement]
  (let [{{:keys [tilewidth tileheight]} :tileset} @map/tilemap
        {:keys [x y] :as leader} (party-leader)]
    (if (and (mob/is-standing-still leader)
             (nil? engagement))
      (cond
        (= last-key :up)
        (start-moving! :up x (- y tileheight))
        (= last-key :down)
        (start-moving! :down x (+ y tileheight))
        (= last-key :left)
        (start-moving! :left (- x tilewidth) y)
        (= last-key :right)
        (start-moving! :right (+ x tilewidth) y)))))

(defn update-player-sprite!
  [time-elapsed-ns]
  (let [leader (party-leader)
        {:keys [sprite] {:keys [current-animation]} :sprite} leader
        animation (get-in sprite [:animations current-animation])
        {:keys [frame delay is-playing]} animation
        next-frame (sprite/get-sprite-frame sprite frame)
        time-elapsed-path [(:identifier leader) :sprite :animations current-animation :time-elapsed]]
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
                (swap! party assoc-in
                       [(:identifier leader) :sprite :animations current-animation :is-playing] false)
                (swap! party assoc-in
                       [(:identifier leader) :sprite :animations current-animation :frame] 0))
              (swap! party assoc-in
                     [(:identifier leader) :sprite :animations current-animation :frame] next-frame))))))))

(defn- get-inspect
  [tile-position dir-1 dir-2 direction-facing tile-size]
  (if (= dir-1 direction-facing)
    (- tile-position tile-size)
    (if (= dir-2 direction-facing)
      (+ tile-position tile-size)
      tile-position)))

(defn get-inspect-coords
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

(defn remove-item!
  ([item-key quantity menu]
   (swap! player update-in [:items item-key] (fn [amount] (- amount quantity)))
   (if (= 0 (get-in @player [:items item-key]))
     (let [menu-index (if menu (ui/get-menu-index menu))
           {{:keys [cursor]} menu-index} @ui/menus]
       (swap! player update-in [:items] dissoc item-key)
       (if (and
            menu
            (= cursor (count (:items @player)))
            (> cursor 0))
         (swap! ui/menus update-in [menu-index :cursor] dec)))))
  ([item-key]
   (remove-item! item-key 1 nil)))

(defn add-item!
  ([item-key]
   (if (contains? (:items @player) item-key)
     (swap! player update-in [:items item-key] inc)
     (swap! player update-in [:items] conj (item/create-inventory-item item-key))))
  ([item-key quantity]
   (doseq [_ (range 0 quantity)]
     (add-item! item-key))))

(defn add-grant!
  [grant]
  (swap! player update-in [:grants] conj grant))
