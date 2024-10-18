(ns cljrpgengine.constants
  (:import (java.awt Color)))

(def target-fps 60)

(def font-family "natural-mono-bold.ttf")

(def text-size 16)

(def line-spacing 24)

(def padding 30)

(def screen-width 640)
(def screen-height 400)

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

(def time-per-frame-nano 200000000)

(def nano-per-second 1000000000)

(def move-delay 14000000)

(def dialog-height 140)

(def resources-dir "resources/")

(def save-dir (str resources-dir "saves/"))

(def sprites-dir (str resources-dir "sprites/"))

(def tile-size 16)
