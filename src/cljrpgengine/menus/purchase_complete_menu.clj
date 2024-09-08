(ns cljrpgengine.menus.purchase-complete-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype PurchaseCompleteMenu [state shop item quantity]
  menu/Menu
  (draw [_]
    (let [x (/ (first constants/window) 5)
          y (/ (second constants/window) 5)
          w (* x 3)
          h (* y 3)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 "Purchase complete!")))
  (cursor-length [_] 0)
  (menu-type [_] :purchase-complete)
  (key-pressed [_]
    (ui/close-menu! state)))

(defn create-menu
  [state shop item quantity]
  (PurchaseCompleteMenu. state shop item quantity))

