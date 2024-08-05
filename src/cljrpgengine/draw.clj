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

;(defn update []
;  (let [elapsed (- (inst-ms (Instant/now)) (:last-frame-change fireas 0))
;        animation (get-in fireas [:animations (:current-animation fireas)])]
;    (if (> elapsed (:delay animation))
;      (update-in fireas :last-frame-change (inst-ms (Instant/now)))))
;  (println fireas))

(defn draw []
  (q/background 255)
  ;(println (q/frame-count))
  ;(println (mod (q/frame-count) 8))
  ;(if (= (mod (q/frame-count) (:delay (:current-animation fireas))) 0)
  ;  (update-in fireas [(:current-animation fireas)] assoc :frame inc 0))
  ;(println @fireas)
  ;(let [animation ]
  ;  (println (get-in [:animations animation] @fireas)))
  ;(println (:current-animation @fireas))

  ;(update-in @fireas [:animations (:current-animation @fireas)] assoc :frame 1)

  ;(if (= (mod (q/frame-count) (:delay (:current-animation fireas))) 0)
  ;  )
  ;(println "animations: " (:animations @fireas))
  ;
  ;(println "a " ((:current-animation @fireas) (:animations @fireas)))
  ;
  ;(System/exit 0)
  ;(println (get-in @fireas [:animations (:current-animation @fireas) :delay]))
  ;(println (mod (q/frame-count) (get-in @fireas [:animations (:current-animation @fireas) :frames])))

  ;(fn [_] (mod (q/frame-count) (get-in @fireas [:animations (:current-animation @fireas) :frames])))
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


  ;(println "b " ((:current-animation @fireas) (:animations @fireas)))
  ;(System/exit 1)

  (sprite/draw @fireas))
