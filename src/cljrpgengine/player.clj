(ns cljrpgengine.player
  (:require [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.menus.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(defn create-new-player
  [x y direction]
  {:party [(mob/create-mob
            :fireas
            "Fireas"
            direction
            x y
            (sprite/create-from-name :fireas direction))]})

(defn get-player-first-mob
  [state]
  (get-in @state [:player :party 0]))

(defn start-moving!
  [state key new-x new-y]
  (if
   (and
    (not
     (mob/blocked-by-mob?
      (get-in @state [:player :party 0])
      (:mobs @state)
      new-x
      new-y
      (get-in @state [:map :tileset :tilewidth])))
    (not
     (map/is-blocking?
      (get-in @state [:map :tilemap])
      (get-in @state [:map :tileset])
      new-x
      new-y)))
    (dosync (alter state update :keys conj key)
            (alter state assoc-in [:player :party 0 :sprite :current-animation] key)
            (alter state assoc-in [:player :party 0 :sprite :animations (keyword key) :is-playing] true)
            (alter state update-in [:player :party 0] assoc
                   :x-offset (- (get-in @state [:player :party 0 :x]) new-x)
                   :y-offset (- (get-in @state [:player :party 0 :y]) new-y)
                   :x new-x
                   :y new-y
                   :direction key))
    (dosync
     (alter state assoc-in [:player :party 0 :sprite :current-animation] key)
     (alter state assoc-in [:player :party 0 :direction] key))))

(defn check-start-moving
  [state]
  (let [mob (get-player-first-mob state)
        x (:x mob)
        y (:y mob)
        keys (:keys @state)
        last-key (first keys)
        tile-width (get-in @state [:map :tileset :tilewidth])
        tile-height (get-in @state [:map :tileset :tileheight])]
    (if (and
         (= 0 (:x-offset mob))
         (= 0 (:y-offset mob))
         (not (:engagement @state))
         (= 0 (count (:menus @state))))
      (do
        (if (= last-key :up)
          (start-moving! state :up x (- y tile-height))
          (if (= last-key :down)
            (start-moving! state :down x (+ y tile-height))
            (if (= last-key :left)
              (start-moving! state :left (- x tile-width) y)
              (if (= last-key :right)
                (start-moving! state :right (+ x tile-width) y)))))))))

(defn update-player-sprite!
  [state]
  (let [mob (get-player-first-mob state)
        sprite (:sprite mob)
        current-animation (:current-animation sprite)]
    (dosync
     (alter
      state
      update-in
      [:player :party 0 :sprite :animations current-animation :frame]
      (fn [frame] (sprite/get-sprite-frame sprite frame)))
     (if (and
          (= 0 (:x-offset mob))
          (= 0 (:y-offset mob)))
       (alter state assoc-in [:player :party 0 :sprite :animations current-animation :is-playing] false)))))

(defn update-move-offsets!
  [state]
  (let [mob (get-player-first-mob state)
        x-offset (:x-offset mob)
        y-offset (:y-offset mob)]
    (dosync
     (if (> 0 x-offset)
       (alter state update-in [:player :party 0 :x-offset] inc)
       (if (< 0 x-offset)
         (alter state update-in [:player :party 0 :x-offset] dec)
         (if (> 0 y-offset)
           (alter state update-in [:player :party 0 :y-offset] inc)
           (if (< 0 y-offset)
             (alter state update-in [:player :party 0 :y-offset] dec))))))))

(defn- change-map!
  [state area-name room entrance-name]
  (let [new-map (map/load-map area-name room)
        entrance (map/get-entrance new-map entrance-name)]
    (dosync
     (alter state assoc-in [:map] new-map)
     (alter state update-in [:player :party 0] assoc
            :x (:x entrance)
            :y (:y entrance))
     (alter state assoc-in [:mobs] #{}))))

(defn check-exits
  [state]
  (let [mob (get-player-first-mob state)
        x-offset (:x-offset mob)
        y-offset (:y-offset mob)]
    (if (and (= 0 y-offset)
             (= 0 x-offset))
      (let [exit (map/get-interaction-from-coords (:map @state) map/get-exits (:x mob) (:y mob))]
        (if exit
          (change-map! state (:scene exit) (:room exit) (:to exit)))))))

(defn- create-engagement!
  [state mob]
  (dosync (alter state assoc
                 :engagement {:dialog (:dialog (event/get-dialog-event! state (:identifier mob)))
                              :dialog-index 0
                              :mob (:identifier mob)
                              :mob-direction (get-in mob [:sprite :current-animation])})
          (alter state assoc-in
                 [:mobs (.indexOf (:mobs @state) mob) :sprite :current-animation]
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

(defn action-engaged!
  [state]
  (let [{:keys [engagement mobs map]
         {[{:keys [direction x y]}] :party} :player
         {{:keys [tilewidth tileheight]} :tileset} :map} @state
        inspect-x (if (= :left direction)
                    (- x  tilewidth)
                    (if (= :right direction)
                      (+ x tilewidth)
                      x))
        inspect-y (if (= :up direction)
                    (- y tileheight)
                    (if (= :down direction)
                      (+ y tileheight)
                      y))
        mob (util/filter-first #(and (= (:x %) inspect-x) (= (:y %) inspect-y)) mobs)]
    (if engagement
      (if (engagement-done? engagement)
        (clear-engagement! state engagement)
        (inc-engagement! state))
      (if mob
        (create-engagement! state mob)
        (if-let [shop (:name (map/get-interaction-from-coords
                               map
                               map/get-shops
                               x
                               y))]
          (ui/open-menu! state (shop-menu/create-menu state shop)))))))
