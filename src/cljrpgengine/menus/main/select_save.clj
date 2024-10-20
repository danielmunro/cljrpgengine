(ns cljrpgengine.menus.main.select-save
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.initialize-game :as new-game]
            [cljrpgengine.ui :as ui]))

(deftype SelectSaveMenu [state save]
  menu/Menu
  (draw [menu]
    (let [padding-x (/ constants/screen-width 10)
          padding-y (/ constants/screen-height 10)
          width (- constants/screen-width (* padding-x 2))
          height (- constants/screen-height (* padding-y 2))
          menu-item (partial ui/draw-line padding-x padding-y)
          cursor (ui/get-menu-cursor state (.menu-type menu))]
      (ui/draw-window padding-x padding-y width height)
      (ui/draw-cursor padding-x padding-y cursor)
      (dorun
       (for [i (range (count (:saves save)))]
         (menu-item i (nth (:saves save) i))))))
  (cursor-length [_]
    (count (:saves save)))
  (menu-type [_] :select-save-menu)
  (key-pressed [menu]
    (new-game/load-save
     state
     (str (:name save) "/" (nth (:saves save) (ui/get-menu-cursor state (.menu-type menu)))))))

(defn create-menu
  [state saves]
  (SelectSaveMenu. state saves))
