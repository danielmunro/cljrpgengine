(ns cljrpgengine.menus.party.quit-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype QuitMenu [state]
  menu/Menu
  (draw [_]
    (let [w (/ (first constants/window) 1.5)
          h (/ (second constants/window) 2)
          x (/ w 4)
          y (/ h 2)
          cursor (ui/get-menu-cursor :quit)]
      (ui/draw-window x y w h)
      (ui/draw-line x y 0 "Unsaved progress will be lost.")
      (ui/draw-line x y 1 "Are you sure?")
      (ui/draw-line x y 3 "No")
      (ui/draw-line x y 4 "Yes")
      (ui/draw-cursor x y (if (= cursor 0) 3 4))))
  (cursor-length [_] 2)
  (menu-type [_] :quit)
  (key-pressed [_]
    (let [cursor (ui/get-menu-cursor :quit)]
      (cond
        (= 0 cursor)
        (ui/close-menu!)
        (= 1 cursor)
        (System/exit 0)))))

(defn create-menu
  [state]
  (QuitMenu. state))
