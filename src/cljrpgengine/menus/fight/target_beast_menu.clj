(ns cljrpgengine.menus.fight.target-beast-menu
  (:require [cljrpgengine.fight :as fight]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(deftype TargetBeastMenu [party-index]
  menu/Menu
  (draw [menu]
    (let [cursor (ui/get-menu-cursor (.menu-type menu))
          {:keys [x y]} (get @fight/encounter cursor)]
      (ui/draw-cursor x
                      y)))
  (cursor-length [_] (count @fight/encounter))
  (menu-type [_] :target-beast)
  (key-pressed [menu]
    (swap! fight/actions
           (fn [actions]
             (conj actions {:action :player-attack
                            :player party-index
                            :beast (ui/get-menu-cursor (.menu-type menu))})))
    (ui/close-menu! 2)))

(defn create-menu
  [party-index]
  (TargetBeastMenu. party-index))
