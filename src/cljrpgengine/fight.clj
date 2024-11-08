(ns cljrpgengine.fight
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]
            [cljrpgengine.window :as window]
            [clojure.java.io :as io]))

(def beastiary (atom {}))
(def encounter (atom nil))
(def room-encounters (atom []))
(def encounter-types (atom {}))
(def background (atom nil))
(def previous-animation (atom nil))
(def actions (atom []))
(def current-action (atom nil))
(def xp-to-gain (atom 0))

(defn set-room-encounters!
  [-room-encounters]
  (swap! room-encounters (fn [_] -room-encounters)))

(defn check-encounter-collision
  [state]
  (let [{{:keys [x y]} :player} @state]
    (util/filter-first
     #(let [b-x (:x %)
            b-y (:y %)
            b-w (:width %)
            b-h (:height %)]
        (util/collision-detected?
         x y
         (+ x (first constants/character-dimensions))
         (+ y (second constants/character-dimensions))
         b-x
         b-y
         (+ b-x b-w)
         (+ b-y b-h))) @room-encounters)))

(defn load-beastiary!
  []
  (let [data (read-string (slurp (str constants/resources-dir "beastiary.edn")))]
    (swap! beastiary
           (fn [_]
             (into {}
                   (map
                    (fn [k]
                      {k (let [beast (get data k)
                               mana (:mana beast 0)]
                           (assoc
                            beast
                            :image (util/load-image (str "beasts/" (:image beast)))
                            :max-hp (:hp beast)
                            :mana mana
                            :max-mana mana))}))
                   (keys data))))))

(defn load-encounters!
  [scene room]
  (let [file-path (str constants/scenes-dir (name scene) "/" (name room) "/encounters.edn")
        file (io/file file-path)]
    (swap! encounter-types (fn [_]
                             (if (.exists file)
                               (read-string (slurp file-path))
                               {})))))

(defn start!
  [state map-encounter]
  (let [enc (rand-nth ((keyword (:encounter map-encounter)) @encounter-types))]
    (swap! encounter
           (fn [_]
             (mapv
              (fn [mob]
                (let [mob-key (first (keys mob))]
                  (assoc (get @beastiary mob-key) :type mob-key
                         :x (int (* (get-in mob [mob-key :x]) constants/screen-width))
                         :y (int (* (get-in mob [mob-key :y]) constants/screen-height)))))
              enc)))
    (swap! background (fn [_] (util/load-image (str "backgrounds/" (:background map-encounter))))))
  (swap! previous-animation (fn [_] (get-in @state [:player :party 0 :sprite :current-animation])))
  (doseq [i (range 0 (count (get-in @state [:player :party])))]
    (dosync
     (alter state assoc-in [:player :party i :sprite :current-animation] :left)
     (alter state assoc-in [:player :party i :sprite :animations :left :frame] 0)))
  (swap! util/player-atb-gauge (fn [_]
                                 (vec (repeatedly (count (get-in @state [:player :party]))
                                                  #(rand-int (/ constants/atb-width 2))))))
  (swap! xp-to-gain (constantly 0)))

(defn- draw-background
  []
  (.drawImage @window/graphics @background 0 0 constants/screen-width constants/screen-height nil))

(defn- draw-beast-status-menu
  []
  (ui/draw-window
   0 (* constants/quarter-height 3)
   constants/quarter-width constants/quarter-height)
  (let [beast-types (into #{} (map #(:type %) @encounter))
        beast-counts (atom (into {} (map (fn [t] {t {:count 0
                                                     :name (:name (util/filter-first (fn [e] (= t (:type e)))
                                                                                     @encounter))}}) beast-types)))]
    (doseq [i (range 0 (count @encounter))]
      (swap! beast-counts update-in [(get-in @encounter [i :type]) :count] inc))
    (let [i (atom 0)]
      (doseq [beast-type (keys @beast-counts)]
        (ui/draw-line 0
                      (* constants/quarter-height 3)
                      @i
                      (str
                       (ui/text-fixed-width (get-in @beast-counts [beast-type :name]) 8)
                       "(" (get-in @beast-counts [beast-type :count]) ")"))
        (swap! i inc)))))

(defn- draw-beasts
  []
  (doseq [beast @encounter]
    (.drawImage @window/graphics
                (:image beast)
                (:x beast)
                (:y beast)
                nil)))

(defn- draw-players
  [state]
  (let [players (get-in @state [:player :party])
        vertical-padding (/ (* constants/quarter-height 3) 4)]
    (doseq [i (range 0 (count players))]
      (sprite/draw (* constants/screen-width 3/4)
                   (+ vertical-padding (- (* i vertical-padding) (* i (second constants/character-dimensions))))
                   (get-in players [i :sprite])))))

(defn- draw-menus
  []
  (draw-beast-status-menu))

(defn draw
  [state]
  (draw-background)
  (draw-menus)
  (draw-beasts)
  (draw-players state))

(defn- update-atb-gauges
  [time-elapsed-ns]
  (doseq [i (range 0 (count @util/player-atb-gauge))]
    (if (< (get @util/player-atb-gauge i) constants/atb-width)
      (do
        (swap! util/player-atb-gauge
               (fn [g]
                 (update-in g [i]
                            (fn [a]
                              (min constants/atb-width (+ a (/ time-elapsed-ns 90000000)))))))))))

(defn- beast-to-target
  []
  (let [target (:beast @current-action)]
    (if (< target (count @encounter))
      target
      0)))

(defn- player-attack
  [state]
  (swap! encounter (fn [e] (update-in e [(beast-to-target) :hp] (fn [hp] (- hp 1)))))
  (swap! util/player-atb-gauge (fn [g] (assoc g (:player @current-action) 0))))

(defn- evaluate-action
  [state]
  (cond
    (= :player-attack (:action @current-action))
    (player-attack state))
  (if (= 0 (get-in @encounter [(beast-to-target) :hp]))
    (swap! xp-to-gain (fn [gain] (+ gain (get-in @encounter [(beast-to-target) :xp]))))
    #_(dosync
      (doseq [i (range 0 (count (get-in @state [:player :party])))]
        (alter state update-in [:player :party i :xp]
               (fn [xp] (+ xp (get-in @encounter [(beast-to-target) :xp])))))))
  (swap! current-action (constantly nil)))

(defn- set-current-action
  []
  (swap! current-action (fn [_] (first @actions)))
  (swap! actions (fn [a] (rest a))))

(defn- remove-dead-beasts
  []
  (swap! encounter
         (fn [enc]
           (filterv #(< 0 (:hp %)) enc))))

(defn- check-end-fight
  [state]
  (if (= 0 (count @encounter))
    (do
      (swap! encounter (constantly nil))
      (ui/close-menu! state))))

(defn update-fight
  [state time-elapsed-ns]
  (update-atb-gauges time-elapsed-ns)
  (if @current-action
    (evaluate-action state)
    (set-current-action))
  (remove-dead-beasts)
  (check-end-fight state))

