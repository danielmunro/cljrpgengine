(ns cljrpgengine.menus.quit-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype QuitMenu [state]
  menu/Menu
  (draw [_]
    (let [w (/ (first constants/window) 2)
          h (/ (second constants/window) 2)
          x (/ w 2)
          y (/ h 2)
          cursor (ui/get-menu-cursor state :quit)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 "Are you sure? All unsaved progress will be lost!")
      (ui/draw-line x y 2 "No")
      (ui/draw-line x y 3 "Yes")
      (ui/draw-cursor x y (if (= cursor 0) 2 3))))
  (cursor-length [_] 2)
  (menu-type [_] :quit)
  (key-pressed [_]
    (let [cursor (ui/get-menu-cursor state :quit)]
      (cond
        (= 0 cursor)
        (ui/close-menu! state)
        (= 1 cursor)
        (System/exit 0)))))

(defn create-menu
  [state]
  (QuitMenu. state))
