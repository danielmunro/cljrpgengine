(ns cljrpgengine.player
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.menus.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]
            [quil.core :as q]))

(defn create-new-player
  [x y direction]
  {:party [(mob/create-mob
            :fireas
            "Fireas"
            :warrior 1
            direction
            x y
            (sprite/create-from-name :fireas direction)
            (q/load-image "portraits/fireas.png"))
           (mob/create-mob
            :fireas
            "Dingus"
            :mage 1
            direction
            x y
            (sprite/create-from-name :fireas direction)
            (q/load-image "portraits/fireas.png"))
           (mob/create-mob
            :fireas
            "Prabble"
            :rogue 1
            direction
            x y
            (sprite/create-from-name :fireas direction)
            (q/load-image "portraits/fireas.png"))
           (mob/create-mob
            :fireas
            "Floodlegor"
            :cleric 1
            direction
            x y
            (sprite/create-from-name :fireas direction)
            (q/load-image "portraits/fireas.png"))]})

(defn start-moving!
  [state key new-x new-y]
  (let [{:keys                     [mobs]
         {[mob] :party}            :player
         {:keys [tileset tilemap]} :map} @state]
    (if
     (and
      (not
       (mob/blocked-by-mob?
        mob
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
      (dosync (alter state update :keys conj key)
              (alter state assoc-in [:player :party 0 :sprite :current-animation] key)
              (alter state assoc-in [:player :party 0 :sprite :animations (keyword key) :is-playing] true)
              (alter state update-in [:player :party 0] assoc
                     :x-offset (- (:x mob) new-x)
                     :y-offset (- (:y mob) new-y)
                     :x new-x
                     :y new-y
                     :direction key))
      (dosync
       (alter state assoc-in [:player :party 0 :sprite :current-animation] key)
       (alter state assoc-in [:player :party 0 :direction] key)))))

(defn check-start-moving
  [state]
  (let [{:keys [keys engagement menus]
         {[{:keys [x y x-offset y-offset]}] :party} :player
         {{:keys [tilewidth tileheight]} :tileset} :map} @state
        last-key (first keys)]
    (if (and
         (= 0 x-offset)
         (= 0 y-offset)
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
  [state]
  (let [{{[{:keys [sprite x-offset y-offset]}] :party} :player} @state
        current-animation (:current-animation sprite)]
    (dosync
     (alter
      state
      update-in
      [:player :party 0 :sprite :animations current-animation :frame]
      (fn [frame] (sprite/get-sprite-frame sprite frame)))
     (if (and
          (= 0 x-offset)
          (= 0 y-offset))
       (alter state assoc-in [:player :party 0 :sprite :animations current-animation :is-playing] false)))))

(defn update-move-offsets!
  [state]
  (let [{{[{:keys [x-offset y-offset]}] :party} :player} @state]
    (cond
      (< x-offset 0)
      (dosync (alter state update-in [:player :party 0 :x-offset] inc))
      (< 0 x-offset)
      (dosync (alter state update-in [:player :party 0 :x-offset] dec))
      (< y-offset 0)
      (dosync (alter state update-in [:player :party 0 :y-offset] inc))
      (< 0 y-offset)
      (dosync (alter state update-in [:player :party 0 :y-offset] dec)))))

(defn- change-map!
  [state area-name room entrance-name]
  (let [new-map (map/load-map area-name room)
        entrance (map/get-entrance new-map entrance-name)]
    (dosync
     (alter state assoc-in [:map] new-map)
     (alter state update-in [:player :party 0] assoc
            :x (:x entrance)
            :y (:y entrance))
     (alter state assoc-in [:mobs] []))))

(defn check-exits
  [state]
  (let [{:keys [map]
         {[{:keys [x y x-offset y-offset]}] :party} :player} @state]
    (if (and (= 0 y-offset)
             (= 0 x-offset))
      (if-let [exit (map/get-interaction-from-coords map (fn [map] (filter #(= "exit" (:type %)) (get-in map [:tilemap :warps]))) x y)]
        (change-map! state (:scene exit) (:room exit) (:to exit))))))

(defn- create-engagement!
  [state mob]
  (dosync (alter state assoc
                 :engagement {:dialog (:dialog (event/get-dialog-event! state (:identifier mob)))
                              :dialog-index 0
                              :mob (:identifier mob)
                              :mob-direction (get-in mob [:sprite :current-animation])})
          (alter state assoc-in
                 [:mobs (:identifier mob) :sprite :current-animation]
                 (util/opposite-direction (get-in @state [:player :party 0 :direction])))))

(defn- engagement-done?
  [engagement]
  (= (:dialog-index engagement) (dec (count (:dialog engagement)))))

(defn- inc-engagement!
  [state]
  (dosync (alter state update-in [:engagement :dialog-index] inc)))

(defn- clear-engagement!
  [state engagement]
  (let [index (util/get-index-of #(= (:mob engagement) (:identifier (% 1))) (:mobs @state))]
    (dosync (alter state assoc-in [:mobs index :sprite :current-animation] (:mob-direction engagement)))
    (dosync (alter state dissoc :engagement))))

(defn- get-inspect
  [tile-position dir-1 dir-2 direction-facing tile-size]
  (if (= dir-1 direction-facing)
    (- tile-position tile-size)
    (if (= dir-2 direction-facing)
      (+ tile-position tile-size)
      tile-position)))

(defn- get-inspect-coords
  [x y direction tilewidth tileheight]
  [(get-inspect x :left :right direction tilewidth)
   (get-inspect y :up :down direction tileheight)])

(defn action-engaged!
  [state]
  (let [{:keys [engagement mobs map]
         {[{:keys [direction x y]}] :party} :player
         {{:keys [tilewidth tileheight]} :tileset} :map} @state
        [inspect-x inspect-y] (get-inspect-coords x y direction tilewidth tileheight)]
    (if engagement
      (if (engagement-done? engagement)
        (clear-engagement! state engagement)
        (inc-engagement! state))
      (if-let [mob (util/filter-first #(and (= (:x %) inspect-x) (= (:y %) inspect-y)) (vals mobs))]
        (create-engagement! state mob)
        (if-let [shop (:name (map/get-interaction-from-coords
                              map
                              #(get-in % [:tilemap :shops])
                              x
                              y))]
          (ui/open-menu! state (shop-menu/create-menu state shop)))))))
