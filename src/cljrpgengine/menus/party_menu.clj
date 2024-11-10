(ns cljrpgengine.menus.party-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.item-menu :as item-menu]
            [cljrpgengine.menus.quit-menu :as quit-menu]))

(deftype PartyMenu [state]
  menu/Menu
  (draw [menu]
    (ui/draw-window 0 0 constants/screen-width constants/screen-height)
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          x (* 3/4 (first constants/window))
          menu-item (partial ui/draw-line x 0)]
      (ui/draw-portraits @player/party)
      (ui/draw-cursor x 0 cursor)
      (menu-item 0 "Items")
      (menu-item 1 "Equipment")
      (menu-item 2 "Magic")
      (menu-item 3 "Quests")
      (menu-item 4 "Save")
      (menu-item 5 "Quit")))
  (cursor-length [_] 6)
  (menu-type [_] :party)
  (key-pressed [menu]
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))]
      (cond
        (= 0 cursor)
        (ui/open-menu! state (item-menu/create-menu state))
        (= 1 cursor)
        (println "equipment")
        (= 2 cursor)
        (println "magic")
        (= 3 cursor)
        (println "quests")
        (= 4 cursor)
        (println "save")
        (= 5 cursor)
        (ui/open-menu! state (quit-menu/create-menu state))))))

(defn create-menu
  [state]
  (PartyMenu. state))

