(ns cljrpgengine.draw
  (:require [quil.core :as q])
  (:require [cljrpgengine.sprite :as sprite]))

(def img (ref nil))
(def fireas (ref nil))

(defn setup []
  (q/frame-rate 60)
  (q/background 0)
  (dosync
    (ref-set img (q/load-image "tinytown.png"))
    (ref-set fireas (sprite/create
                       :fireas
                       "fireas.png"
                       16
                       24
                       {:down {:frames 4
                               :delay 8
                               :y-offset 0}
                        :left {:frames 4
                               :delay 8
                               :y-offset 1}
                        :right {:frames 4
                                :delay 8
                                :y-offset 2}
                        :up {:frames 4
                             :delay 8
                             :y-offset 3}
                        :sleep {:frames 1
                                :delay 0
                                :y-offset 4}}
                       :down))
    {:player fireas}))

(defn update-frame
  [current-frame total-frames]
  (let [next-frame (inc current-frame)]
    (if (< next-frame total-frames)
      next-frame
      0)))

(defn draw [state]
  (let [current-animation (:current-animation @fireas)
        animation (get-in @fireas [:animations current-animation])]
    (if (= 0 (mod (q/frame-count) (:delay animation)))
      (dosync
        (alter
          fireas
          update-in
          [:animations current-animation :frame]
          (fn [current-frame] (update-frame current-frame (:frames animation)))))))
  (sprite/draw @fireas))
