(ns cljrpgengine.menus.shop.sale-complete-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype SaleCompleteMenu [item quantity]
  menu/Menu
  (draw [_]
    (let [x (/ (first constants/window) 5)
          y (/ (second constants/window) 5)
          w (* x 3)
          h (* y 3)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 "Sale complete!")))
  (cursor-length [_] 0)
  (menu-type [_] :sale-complete)
  (key-pressed [_]
    (ui/close-menu!)))

(defn create-menu
  [item quantity]
  (SaleCompleteMenu. item quantity))
