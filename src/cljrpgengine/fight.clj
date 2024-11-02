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
(def quarter-height (/ constants/screen-height 4))
(def quarter-width (/ constants/screen-width 4))
(def previous-animation (atom nil))

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
                           (merge
                            beast
                            {:image (util/load-image (str "beasts/" (:image beast)))
                             :max-hp (:hp beast)
                             :mana mana
                             :max-mana mana}))}))
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
     (alter state assoc-in [:player :party i :sprite :animations :left :frame] 0))))

(defn- draw-background
  []
  (.drawImage @window/graphics @background 0 0 constants/screen-width constants/screen-height nil))

(defn- draw-beast-status-menu
  []
  (ui/draw-window
   0 (* quarter-height 3)
   quarter-width quarter-height)
  (let [beast-types (into #{} (map #(:type %) @encounter))
        beast-counts (atom (into {} (map (fn [t] {t {:count 0
                                                     :name (:name (util/filter-first (fn [e] (= t (:type e)))
                                                                                     @encounter))}}) beast-types)))]
    (doseq [i (range 0 (count @encounter))]
      (swap! beast-counts update-in [(get-in @encounter [i :type]) :count] inc))
    (let [i (atom 0)]
      (doseq [beast-type (keys @beast-counts)]
        (ui/draw-line 0
                      (* quarter-height 3)
                      @i
                      (str
                       (ui/text-fixed-width (get-in @beast-counts [beast-type :name]) 8)
                       "(" (get-in @beast-counts [beast-type :count]) ")"))
        (swap! i inc)))))

(defn- draw-player-status-menu
  [state]
  (ui/draw-window
   quarter-width (* quarter-height 3)
   (* 3 quarter-width) quarter-height)
  (let [party (get-in @state [:player :party])]
    (doseq [p (range 0 (count party))]
      (ui/draw-line quarter-width
                    (* quarter-height 3)
                    p
                    (str (ui/text-fixed-width (:name (get party p)) 15)
                         (ui/text-fixed-width (str (:hp (get party p)) "/" (:max-hp (get party p))) 10)
                         (:mana (get party p)) "/" (:max-mana (get party p)))))))

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
        vertical-padding (/ (* quarter-height 3) 4)]
    (doseq [i (range 0 (count players))]
      (sprite/draw (* constants/screen-width 3/4)
                   (+ vertical-padding (- (* i vertical-padding) (* i (second constants/character-dimensions))))
                   (get-in players [i :sprite])))))

(defn- draw-menus
  [state]
  (draw-beast-status-menu)
  (draw-player-status-menu state))

(defn draw
  [state]
  (draw-background)
  (draw-menus state)
  (draw-beasts)
  (draw-players state))

(defn update-fight
  [state])
