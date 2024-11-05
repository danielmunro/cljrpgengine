(ns cljrpgengine.menus.fight.target-beast-menu
  (:require [cljrpgengine.fight :as fight]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype TargetBeastMenu [state]
  menu/Menu
  (draw [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          {:keys [x y]} (get @fight/encounter cursor)]
      (ui/draw-cursor x
                      y)))
  (cursor-length [_] (count @fight/encounter))
  (menu-type [_] :target-beast)
  (key-pressed [menu]))

(defn create-menu
  [state]
  (TargetBeastMenu. state))
