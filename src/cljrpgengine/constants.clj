(ns cljrpgengine.constants
  (:import (com.badlogic.gdx.graphics Color)))

(def font-colors {:default Color/WHITE
                  :disabled Color/GRAY
                  :highlight Color/YELLOW})

(def screen-width 640)
(def screen-height 400)

(def mob-width 16)
(def mob-height 24)

(def walk-animation-speed (float 0.1))

(def starting-money 100)

(def resources-dir "resources/")
(def sprites-dir (str resources-dir "sprites/"))
(def scenes-dir (str resources-dir "scenes/"))
(def font-file (str resources-dir "natural-mono-bold.ttf"))

(def tile-size 16)

(def font-size 20)
