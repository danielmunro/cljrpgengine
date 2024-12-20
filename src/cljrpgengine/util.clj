(ns cljrpgengine.util
  (:import (com.badlogic.gdx.scenes.scene2d Actor)))

(def attribute-order [:str :int :wis :dex :con :cha :hp :mana :hit :dam :ac-slash :ac-bash :ac-pierce :ac-magic])

(defn opposite-direction
  [direction]
  (case direction
    :up
    :down
    :down
    :up
    :left
    :right
    :right
    :left))

(defn round1
  [n]
  (/ (Math/round ^float (* n 10)) 10))

(defn create-image
  [texture x y]
  (doto (proxy [Actor] []
          (draw [batch _]
            (.setColor batch (float 1) (float 1) (float 1) (float 255))
            (.draw batch texture (proxy-super getX) (proxy-super getY))))
    (.setWidth (.getWidth texture))
    (.setHeight (.getHeight texture))
    (.setX x)
    (.setY y)))
