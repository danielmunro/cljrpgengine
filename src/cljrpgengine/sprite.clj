(ns cljrpgengine.sprite
  (:require [quil.core :as q]))

;(def graphics (memoize (fn [w h] (q/create-graphics w h))))

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
        y (* height (:y-offset animation))]
    (q/background 255 255 255)
    (q/clip x y width height)
    (q/image image 0 0)))

;(defn offset-pos
;  [[x y] w h]
;  [(- x (/ w 2))
;   (- y (/ h 2))])

;(defn draw
;  [{:keys [width height image]}]
;  (let [[x y] (offset-pos [0 0] width height)]
;    (q/image image x y)))

;(defn draw
;  [sprite]
;  (let [w (:width sprite)
;        h (:height sprite)
;        g (graphics w h)]
;    (println w ", " h)
;    (q/with-graphics g
;                     (.clear g)
;                     (q/image (:image sprite) 0 0)
;                     (.dispose g))))
