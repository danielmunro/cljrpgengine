(ns cljrpgengine.player
  (:require [cljrpgengine.effect :as effect]
            [cljrpgengine.event :as event]
            [cljrpgengine.map :as map]
            [cljrpgengine.menus.shop-menu :as shop-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.prefab-sprites :as prefab-sprites]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(defn create-new-player
  []
  {:party [(mob/create-mob
            :fireas
            "Fireas"
            :warrior 1
            :down 0 0
            (prefab-sprites/create-from-name :edwyn :down)
            (util/load-image "portraits/edwyn.png"))
           (mob/create-mob
            :fireas
            "Dingus"
            :mage 1
            :down 0 0
            (prefab-sprites/create-from-name :edwyn :down)
            (util/load-image "portraits/edwyn.png"))
           (mob/create-mob
            :fireas
            "Prabble"
            :rogue 1
            :down 0 0
            (prefab-sprites/create-from-name :edwyn :down)
            (util/load-image "portraits/edwyn.png"))
           (mob/create-mob
            :fireas
            "Floodlegor"
            :cleric 1
            :down 0 0
            (prefab-sprites/create-from-name :edwyn :down)
            (util/load-image "portraits/edwyn.png"))]
   :x     0
   :y     0
   :x-offset 0
   :y-offset 0
   :direction :down})

(defn load-player
  [data]
  (let [player (create-new-player)
        {{:keys [x y direction]} :player} data]
    (-> player
        (assoc :x x
               :y y
               :direction direction)
        (assoc-in [:party 0 :sprite :current-animation]
                  direction))))

(defn start-moving!
  [state key new-x new-y]
  (let [{:keys [mobs]
         {:keys [x y]} :player
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
      (dosync (alter state update :keys conj key)
              (alter state assoc-in [:player :party 0 :sprite :current-animation] key)
              (alter state assoc-in [:player :party 0 :sprite :animations (keyword key) :is-playing] true)
              (alter state update-in [:player] assoc
                     :x-offset (- x new-x)
                     :y-offset (- y new-y)
                     :x new-x
                     :y new-y
                     :direction key))
      (dosync
       (alter state assoc-in [:player :party 0 :sprite :current-animation] key)
       (alter state assoc-in [:player :direction] key)))))

(defn check-start-moving
  [state]
  (let [{:keys [keys engagement menus player]
         {{:keys [tilewidth tileheight]} :tileset} :map
         {:keys [x y]} :player} @state
        last-key (first keys)]
    (if (and
         (mob/no-move-offset player)
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
  (let [{{:keys [x-offset y-offset]
          [{:keys [sprite] {:keys [current-animation]} :sprite}] :party} :player} @state]
    (mob/update-sprite!
     state
     [:player :party 0 :sprite :animations current-animation]
     {:x-offset x-offset
      :y-offset y-offset
      :sprite sprite})))

(defn update-move-offset!
  [state elapsed-nano]
  (let [{{:keys [x-offset y-offset]} :player} @state]
    (mob/update-move-offset! state x-offset y-offset [:player] [:player :party 0 :sprite] elapsed-nano)))

(defn- change-map!
  [state area-name room entrance-name]
  (let [new-map (map/load-render-map area-name room)
        {:keys [x y]} (map/get-entrance new-map entrance-name)]
    (effect/add-fade-in state)
    (dosync
     (alter state assoc-in [:map] new-map)
     (alter state update-in [:player] assoc
            :x x
            :y y))))

(defn check-exits
  [state]
  (let [{:keys [map player]} @state
        {:keys [x y]} player]
    (if (mob/no-move-offset player)
      (if-let [exit
               (map/get-interaction-from-coords
                map
                (fn [map] (filter #(= "exit" (:type %)) (get-in map [:tilemap :warps])))
                x y)]
        (change-map! state (:scene exit) (:room exit) (:to exit))))))

(defn- create-engagement!
  [state mob]
  (let [identifier (:identifier mob)
        event (event/get-dialog-event! state identifier)]
    (dosync (alter state assoc
                   :engagement {:dialog (:dialog event)
                                :dialog-index 0
                                :mob identifier
                                :event event
                                :mob-direction (get-in mob [:sprite :current-animation])})
            (alter state assoc-in
                   [:mobs identifier :sprite :current-animation]
                   (util/opposite-direction (get-in @state [:player :direction]))))))

(defn- engagement-done?
  [engagement]
  (= (:dialog-index engagement) (dec (count (:dialog engagement)))))

(defn- inc-engagement!
  [state]
  (dosync (alter state update-in [:engagement :dialog-index] inc)))

(defn- clear-engagement!
  [state engagement]
  (let [{:keys [mob mob-direction] {:keys [outcomes]} :event} engagement
        current-animation-path [:mobs mob :sprite :current-animation]]
    (let [current-animation (get-in @state current-animation-path)]
      (event/apply-outcomes! state outcomes)
      (dosync
       (if (= current-animation (get-in @state current-animation-path))
         (alter state assoc-in current-animation-path mob-direction))
       (alter state dissoc :engagement)))))

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
