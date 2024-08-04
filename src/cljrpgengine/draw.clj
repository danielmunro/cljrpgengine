(ns cljrpgengine.draw
  (:require [quil.core :as q])
  (:require [cljrpgengine.sprite :as sprite])
  (:import (java.time Instant)))

(def img (ref nil))
(def fireas (ref nil))

(defn setup []
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
                       :down))))

(defn update []
  (println "test")
  (let [elapsed (- (inst-ms (Instant/now)) (:last-frame-change fireas 0))
        animation (get-in fireas [:animations (:current-animation fireas)])]
    (if (> elapsed (:delay animation))
      (update-in fireas :last-frame-change (inst-ms (Instant/now)))))
  (println fireas))

(defn draw []
  (q/background 255)
  (sprite/draw @fireas))
