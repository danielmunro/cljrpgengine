(ns cljrpgengine.sprite
  (:require [quil.core :as q]))

(def create-graphics (memoize (fn [w h] (q/create-graphics w h))))

(defn add-default-props
  [animations]
  (into {}
        (map
          (fn[animation] {animation (assoc (animation animations) :frame 0 :is-playing false)})
          (keys animations))))

(defn create
  [name filename width height current-animation animations]
  {:name name
   :filename filename
   :image (q/load-image filename)
   :width width
   :height height
   :current-animation current-animation
   :animations (add-default-props animations)})

(defn draw
  [{:keys [width height image animations current-animation]}]
  (apply q/background [0])
  (let [animation (current-animation animations)
        frame (:frame animation 0)
        x (* frame width)
        y (* height (:y-offset animation))
        g (create-graphics width height)]
    (q/with-graphics g
                     (.clear g)
                     (q/image image (- x) (- y)))
    (q/image g 0 0 (* 2 width) (* 2 height))))
