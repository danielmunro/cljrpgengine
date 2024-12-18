(ns cljrpgengine.menu.common
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]
            [clojure.math :as math]))

(def portrait-width 60)
(def portrait-height 80)

(defn get-xp-to-level
  [xp]
  (loop [i 0
         remaining-xp xp]
    (let [xp-for-level (math/round (* 100 (* i (/ i 2))))
          xp-minus-level (- remaining-xp xp-for-level)]
      (if (< xp-minus-level 0)
        (- xp-for-level remaining-xp)
        (recur (inc i) xp-minus-level)))))

(defn draw-portraits
  [window]
   (let [i (atom 0)]
     (doall
      (map
       (fn [identifier]
         (let [portrait-x (+ portrait-width (* 2 constants/padding))
               portrait-y (-> (* constants/padding @i)
                              (- (* portrait-height @i))
                              (- (* constants/padding @i)))
               mob (get @player/party identifier)
               {:keys [hp mana portrait name xp level]} mob
               max-hp (mob/calc-attr mob :hp)
               max-mana (mob/calc-attr mob :mana)
               image (util/create-image
                      portrait
                      (+ constants/padding 20)
                      (- (+ portrait-y (ui/line-number window 1)) (* constants/padding 2)))]
           (.addActor window image)
           (.addActor window
                      (ui/create-label name portrait-x (+ portrait-y (ui/line-number window 1))))
           (.addActor window
                      (ui/create-label
                        (format "%d/%d HP" @hp max-hp)
                        portrait-x (+ portrait-y (ui/line-number window 2))))
           (.addActor window
                      (ui/create-label
                        (format "%d/%d Mana" @mana max-mana)
                        portrait-x (+ portrait-y (ui/line-number window 3))))
           (.addActor window
                      (ui/create-label
                       (format "level %d" @level)
                       (+ portrait-x 200)
                       (+ portrait-y (ui/line-number window 2))))
           (.addActor window
                      (ui/create-label
                       (format "%d xp to level" (get-xp-to-level @xp))
                       (+ portrait-x 200)
                       (+ portrait-y (ui/line-number window 3))))
           (swap! i inc)
           image))
       (keys @player/party)))))

(defn draw-attributes
  ([window pane mob attr-diff]
   (doseq [label (.getChildren pane)]
     (.removeActor pane label))
   (.addActor pane (ui/create-label "Attributes:"
                                    constants/padding
                                    (ui/line-number window 1)))
   (let [i (atom 2)]
     (doseq [attr util/attribute-order]
       (.addActor pane (ui/create-label (str (ui/text-fixed-width (name attr) 10) (mob/calc-attr mob attr))
                                        constants/padding
                                        (ui/line-number window (swap! i inc))))
       (when-let [diff (get attr-diff attr)]
         (if (not= 0 diff)
           (.addActor pane (ui/create-label (str (cond
                                                   (< 0 diff)
                                                   (str "+" diff)
                                                   (< diff 0)
                                                   (str diff)))
                                            (+ constants/padding 120)
                                            (ui/line-number window @i)
                                            (cond
                                              (< 0 diff)
                                              (:success constants/font-colors)
                                              (< diff 0)
                                              (:danger constants/font-colors)))))))))
  ([window pane mob]
   (draw-attributes window pane mob nil)))
