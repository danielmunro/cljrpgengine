(ns cljrpgengine.ui
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps])
  (:import (com.badlogic.gdx.graphics Color Pixmap Pixmap$Format Texture)
           (com.badlogic.gdx.scenes.scene2d Actor Group)
           (com.badlogic.gdx.scenes.scene2d.ui Label Label$LabelStyle)))

(defn create-label
  ([text x y]
   (let [style (Label$LabelStyle. @deps/font Color/WHITE)]
     (doto (Label. ^CharSequence text style)
       (.setX x)
       (.setY y))))
  ([text]
   (create-label text 0 0)))

(defn- create-texture
  [color]
  (let [pixmap (Pixmap. 1 1 Pixmap$Format/RGBA8888)]
    (.setColor pixmap ^Color color)
    (.fillRectangle pixmap 0 0 1 1)
    (let [tex (Texture. pixmap)]
      (.dispose pixmap)
      tex)))

(defn center-in-window
  [window actor]
  (doto actor
    (.setX (/ (- (.getWidth window) (.getWidth actor)) 2))))

(defn create-window
  [x y width height]
  (let [blue-texture (create-texture Color/BLUE)
        white-texture (create-texture Color/WHITE)
        group (doto (Group.)
                (.setX (float x))
                (.setY (float y))
                (.setWidth (float width))
                (.setHeight (float height)))]
    (.addActor group
               (doto (proxy [Actor] []
                       (draw [batch _]
                         (.draw batch
                                white-texture
                                (proxy-super getX)
                                (proxy-super getY)
                                (proxy-super getWidth)
                                (proxy-super getHeight))
                         (.draw batch
                                blue-texture
                                (float (inc (proxy-super getX)))
                                (float (inc (proxy-super getY)))
                                (float (- (proxy-super getWidth) 2))
                                (float (- (proxy-super getHeight) 2)))))
                 (.setX 0)
                 (.setY 0)
                 (.setWidth (float width))
                 (.setHeight (float height))))
    group))

(defn create-cursor
  []
  (let [cursor (Texture. (str constants/sprites-dir "cursor.png"))]
    {:index (atom 0)
     :actor (doto (proxy [Actor] []
                    (draw [batch _]
                      (.setColor batch (float 1) (float 1) (float 1) (float 255))
                      (.draw batch cursor (proxy-super getX) (proxy-super getY))))
              (.setWidth (.getWidth cursor))
              (.setHeight (.getHeight cursor)))}))

(defn line-number
  [window line-number]
  (- (.getHeight window)
     (* line-number (* constants/font-size 1.5))))
