(ns cljrpgengine.constants
  (:import (java.awt Color)))

(def target-fps 60)

(def font-family "natural-mono-bold.ttf")

(def text-size 14)

(def line-spacing 21)

(def padding 10)

(def screen-width 640)
(def screen-height 400)

(def quarter-height (/ screen-height 4))
(def quarter-width (/ screen-width 4))

(def window [screen-width screen-height])

(def draw-blocking false)

(def character-dimensions [16 24])

(def colors {:window        Color/BLUE
             :font-default  Color/WHITE
             :font-disabled Color/GRAY})

(def dialog-text-width 62)

(def item-name-width 30)

(def cost-width 6)

(def starting-money 100)

(def portrait-size [40 40])

(def nano-per-second 1000000000)

(def animation-delay-ns 1000000)

(def move-delay-ns 14000000)

(def dialog-height 140)

(def resources-dir "resources/")

(def save-dir (str resources-dir "saves/"))

(def sprites-dir (str resources-dir "sprites/"))

(def tilesets-dir (str resources-dir "tilesets/"))

(def scenes-dir (str resources-dir "scenes/"))

(def beasts-dir (str resources-dir "beasts/"))

(def backgrounds-dir (str resources-dir "backgrounds/"))

(def portraits-dir (str resources-dir "portraits/"))

(def tile-size 16)

(def atb-width 64)
