(ns cljrpgengine.sprite
  (:require [quil.core :as q]))

(def create-graphics (memoize (fn [w h] (q/create-graphics w h))))

;(defn add-frame
;  [animations]
;  (println "args: " animations)
;  (println (keys animations))
;  (reduce (fn [animation k] (println animation " :: " k)) animations))

(defn create
  [name filename width height animations current-animation]
  {:name name
   :filename filename
   :image (q/load-image filename)
   :width width
   :height height
   :animations animations
   :current-animation current-animation})

(defn draw
  [{:keys [width height image animations current-animation]}]
  (let [animation (get animations current-animation)
        frame (:frame animation 0)
        x (* frame width)
        y (* height (:y-offset animation))
        g (create-graphics width height)]
    (q/with-graphics g
                     (.clear g)
                     (q/image image (- x) (- y)))
    (q/image g 0 0 (* 2 width) (* 2 height))
    ))
