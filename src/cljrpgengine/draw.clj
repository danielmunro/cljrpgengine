(ns cljrpgengine.draw
  (:require [quil.core :as q])
  (:require [cljrpgengine.sprite :as sprite])
  (:import (java.time Instant)))

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
                               :y-offset 0
                               :frame 0}
                        :left {:frames 4
                               :delay 8
                               :y-offset 1
                               :frame 0}
                        :right {:frames 4
                                :delay 8
                                :y-offset 2
                                :frame 0}
                        :up {:frames 4
                             :delay 8
                             :y-offset 3
                             :frame 0}
                        :sleep {:frames 1
                                :delay 0
                                :y-offset 4
                                :frame 0}}
                       :down))))

(defn draw []
  (q/background 255)
  (if (= 0 (mod (q/frame-count) (get-in @fireas [:animations (:current-animation @fireas) :delay])))
    (dosync
      (alter
        fireas
        update-in
        [:animations (:current-animation @fireas) :frame]
        inc)))
  (if (>= (get-in @fireas [:animations (:current-animation @fireas) :frame]) (get-in @fireas [:animations (:current-animation @fireas) :frames]))
    (dosync
      (alter fireas update-in [:animations (:current-animation @fireas) :frame] (fn [_] 0))))

  (sprite/draw @fireas))
