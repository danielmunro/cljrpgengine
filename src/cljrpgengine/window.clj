(ns cljrpgengine.window
  (:require [cljrpgengine.constants :as constants])
  (:import (java.awt Color GraphicsEnvironment Toolkit)
           (java.awt.event KeyListener)
           (javax.swing JFrame)))

(def graphics (atom nil))
(def ratio-x (atom nil))
(def ratio-y (atom nil))

(defn create
  [width height key-pressed key-released]
  (let [screenSize (.getScreenSize (Toolkit/getDefaultToolkit))
        m (min
           (/ (.getWidth screenSize) constants/screen-width)
           (/ (.getHeight screenSize) constants/screen-height))]
    (swap! ratio-x (fn [_] m))
    (swap! ratio-y (fn [_] m)))
  (let [frame (JFrame.)]
    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.setSize frame width height)
    (-> (.getContentPane frame)
        (.setBackground Color/BLACK))
    (.setExtendedState frame JFrame/MAXIMIZED_BOTH)
    (.setFullScreenWindow (.getDefaultScreenDevice (GraphicsEnvironment/getLocalGraphicsEnvironment)) frame)
    (.setVisible frame true)
    (.createBufferStrategy frame 2)
    (.addKeyListener
     frame
     (proxy
      [KeyListener]
      []
       (keyPressed [e]
         (key-pressed e))
       (keyReleased [e]
         (key-released e))
       (keyTyped [_])))
    frame))

(defn fill-screen
  [color]
  (let [g @graphics]
    (.setColor g color)
    (.fillRect g 0 0 (* 2 constants/screen-width) (* 2 constants/screen-height))))

(defn draw-graphics
  [buffer-strategy]
  (.show buffer-strategy))

(defn new-graphics
  [bs]
  (let [g (.getDrawGraphics bs)]
    (.scale g @ratio-x @ratio-y)
    (swap! graphics (constantly g))
    (fill-screen Color/BLACK)))

