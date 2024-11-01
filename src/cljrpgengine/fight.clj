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
  (let [enc ((keyword (:encounter map-encounter)) @encounter-types)]
    (swap! encounter
           (fn [_]
             (into {} (map
                       (fn [k]
                         (let [max (:max (get enc k))
                               min (:min (get enc k))]
                           {k (mapv (fn [_] (get @beastiary k)) (range (+ (rand-int (- max min)) min)))}))
                       (keys enc)))))
    (swap! background (fn [_] (util/load-image (str "backgrounds/" (:background map-encounter))))))
  (swap! previous-animation (fn [_] (get-in @state [:player :party 0 :sprite :current-animation])))
  (dorun
   (for [i (range 0 (count (get-in @state [:player :party])))]
     (dosync
      (alter state assoc-in [:player :party i :sprite :current-animation] :left)
      (alter state assoc-in [:player :party i :sprite :animations :left :frame] 0))))
  #_(println @encounter)
  #_(System/exit 1))

(defn- draw-background
  []
  (.drawImage @window/graphics @background 0 0 constants/screen-width constants/screen-height nil))

(defn- draw-beast-status-menu
  []
  (ui/draw-window
   0 (* quarter-height 3)
   quarter-width quarter-height)
  (let [beast-types (vec (keys @encounter))]
    (dorun
     (for [i (range 0 (count beast-types))]
       (let [beast-type (get beast-types i)
             beasts (get @encounter beast-type)]
         (ui/draw-line 0
                       (* quarter-height 3)
                       i
                       (str (ui/text-fixed-width (:name (get beasts 0)) 10) "(" (count beasts) ")")))))))

(defn- draw-player-status-menu
  [state]
  (ui/draw-window
   quarter-width (* quarter-height 3)
   (* 3 quarter-width) quarter-height)
  (let [party (get-in @state [:player :party])]
    (dorun
     (for [p (range 0 (count party))]
       (ui/draw-line quarter-width
                     (* quarter-height 3)
                     p
                     (str (ui/text-fixed-width (:name (get party p)) 20)
                          (ui/text-fixed-width (str (:hp (get party p)) "/" (:max-hp (get party p))) 10)
                          (:mana (get party p)) "/" (:max-mana (get party p))))))))

(defn- draw-beasts
  []
  (let []
    (dorun
     (for [beast-key (vec (keys @encounter))]
       (dorun
        (let [beast-count (count (get @encounter beast-key))]
          (for [i (range 0 beast-count)]
            (let [beast (get-in @encounter [beast-key i])]
              (.drawImage @window/graphics
                          (:image beast)
                          100
                          (* i (.getHeight (:image beast)))
                          nil)))))))))

(defn- draw-players
  [state]
  (let [players (get-in @state [:player :party])
        vertical-padding (/ (* quarter-height 3) 4)]
    (dorun
     (for [i (range 0 (count players))]
       (sprite/draw (* constants/screen-width 3/4)
                    (+ vertical-padding (- (* i vertical-padding) (* i (second constants/character-dimensions))))
                    (get-in players [i :sprite]))))))

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
