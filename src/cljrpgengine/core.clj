(ns cljrpgengine.core
  (:require [quil.core :as q])
  (:require [cljrpgengine.draw :as draw])
  (:require [quil.middleware :as m])
  (:import (java.awt.event KeyEvent)))

(defn set-current-animation
  [state animation]
  (dosync (alter (:player state) update :current-animation (fn [_] animation))))

(defn update-state
  [state]
  (assoc state :key-code (q/key-code))
  (cond
    (= (KeyEvent/VK_UP) (q/key-code))
    (set-current-animation state :up)
    (= (KeyEvent/VK_DOWN) (q/key-code))
    (set-current-animation state :down)
    (= (KeyEvent/VK_LEFT) (q/key-code))
    (set-current-animation state :left)
    (= (KeyEvent/VK_RIGHT) (q/key-code))
    (set-current-animation state :right))
  state)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "starting game...")
  (q/defsketch hello
               :setup draw/setup
               :size [300 300]
               :update update-state
               :draw draw/draw
               :middleware [m/fun-mode]))
