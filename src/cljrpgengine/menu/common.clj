(ns cljrpgengine.menu.common
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [clojure.math :as math]))

(def portrait-width 40)
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

(defn restore-amount
  [modifier amount max-amount]
  (min modifier (- max-amount amount)))

(defn draw-portraits
  ([window item selected-mob]
   (doseq [i (range 0 (count @player/party))]
     (let [identifier (nth (keys @player/party) i)
           portrait-x 50
           portrait-y (-> (* 10 i)
                          (- (* portrait-height i))
                          (- (* constants/padding i)))
           {:keys [affect amount]} item
           {{:keys [hp max-hp mana max-mana portrait name xp level]} identifier} @player/party
           amount-hp (if (= :restore-hp affect) (restore-amount amount hp max-hp))
           amount-mana (if (= :restore-mana affect) (restore-amount amount mana max-mana))]
       ;(.drawImage @window/graphics (:image portrait) constants/padding (+ 20 portrait-y) nil)
       (.addActor window
                  (ui/create-label name portrait-x (+ portrait-y (ui/line-number window 1))))
       (.addActor window
                  (ui/create-label
                   (str (format "%d/%d HP" @hp @max-hp)
                        (if (and (= i selected-mob)
                                 (= :restore-hp affect))
                          (format " +%d" amount-hp)))
                   portrait-x (+ portrait-y (ui/line-number window 2))))
       (.addActor window
                  (ui/create-label
                   (str (format "%d/%d Mana" @mana @max-mana)
                        (if (and (= i selected-mob)
                                 (= :restore-mana affect))
                          (format " +%d" amount-mana)))
                   portrait-x (+ portrait-y (ui/line-number window 3))))
       (.addActor window
                  (ui/create-label
                   (format "level %d" level)
                   (+ portrait-x 200)
                   (+ portrait-y (ui/line-number window 2))))
       (.addActor window
                  (ui/create-label
                   (format "%d xp to level" (get-xp-to-level xp))
                   (+ portrait-x 200)
                   (+ portrait-y (ui/line-number window 3)))))))
  ([window]
   (draw-portraits window nil -1)))
