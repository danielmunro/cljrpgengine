(ns cljrpgengine.core
  (:require [quil.core :as q])
  (:require [cljrpgengine.draw :as draw])
  (:require [cljrpgengine.sprite :as sprite]
            [quil.middleware :as m])
  (:import (java.awt.event KeyEvent)))

(def img (ref nil))
(def fireas (ref nil))

(defn setup []
  (q/frame-rate 60)
  (q/background 0)
  (dosync
    ;(ref-set img (q/load-image "tinytown.png"))
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
                      :down))
    {:player fireas}))

(defn set-current-animation
  [state animation]
  (dosync (alter (:player state) update :current-animation (fn [_] animation))))

(defn update-frame
  [current-frame total-frames]
  (let [next-frame (inc current-frame)]
    (if (< next-frame total-frames)
      next-frame
      0)))

(defn update-animation-frame
  [state]
  (let [current-animation (:current-animation @fireas)
        animation (get-in @fireas [:animations current-animation])]
    (if (= 0 (mod (q/frame-count) (:delay animation)))
      (dosync
        (alter
          fireas
          update-in
          [:animations current-animation :frame]
          (fn [current-frame] (update-frame current-frame (:frames animation))))))))

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
  (update-animation-frame state)
  state)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "starting game...")
  (q/defsketch hello
               :setup setup
               :size [300 300]
               :update update-state
               :draw draw/draw
               :middleware [m/fun-mode]))
