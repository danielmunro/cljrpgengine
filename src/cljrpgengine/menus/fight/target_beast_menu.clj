(ns cljrpgengine.menus.fight.target-beast-menu
  (:require [cljrpgengine.fight :as fight]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype TargetBeastMenu [state party-index]
  menu/Menu
  (draw [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          {:keys [x y]} (get @fight/encounter cursor)]
      (if (and x y)
        (ui/draw-cursor x
                        y))))
  (cursor-length [_] (count @fight/encounter))
  (menu-type [_] :target-beast)
  (key-pressed [menu]
    (swap! fight/actions
           (fn [actions]
             (conj actions {:action :player-attack
                            :player party-index
                            :beast (ui/get-menu-cursor state (.menu-type menu))})))
    (ui/close-menu! state 2)))

(defn create-menu
  [state party-index]
  (TargetBeastMenu. state party-index))
