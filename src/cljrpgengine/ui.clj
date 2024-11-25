(ns cljrpgengine.ui
  (:require [cljrpgengine.deps :as deps])
  (:import (com.badlogic.gdx.graphics Color Pixmap Pixmap$Format Texture)
           (com.badlogic.gdx.scenes.scene2d Actor)
           (com.badlogic.gdx.scenes.scene2d.ui Label Label$LabelStyle)))

(defn create-label
  ([text x y]
   (let [style (Label$LabelStyle. @deps/font Color/WHITE)
         label (doto (Label. ^CharSequence text style))]
     (doto label
       (.setX x)
       (.setY y))))
  ([text]
   (create-label text 0 0)))

(defn- create-texture
  [width height color]
  (let [pixmap (Pixmap. ^int width ^int height Pixmap$Format/RGBA8888)]
    (.setColor pixmap ^Color color)
    (.fillRectangle pixmap 0 0 width height)
    (let [tex (Texture. pixmap)]
      (.dispose pixmap)
      tex)))

(defn create-window
  [x y width height]
  (let [color Color/BLUE
        tex (create-texture width height color)
        fx (float x)
        fy (float y)
        fw (float width)
        fh (float height)]
    (proxy [Actor] []
      (draw [batch alpha]
        (.setColor batch (.r color) (.g color) (.b color) (* (.a color) alpha))
        (.draw batch tex fx fy fw fh)))))
