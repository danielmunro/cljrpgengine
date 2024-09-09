(ns cljrpgengine.menus.party-menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.item-menu :as item-menu]
            [cljrpgengine.menus.quit-menu :as quit-menu]
            [quil.core :as q]))

(deftype PartyMenu [state]
  menu/Menu
  (draw [menu]
    (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (let [cursor (ui/get-menu-cursor state (.menu-type menu))
          x (* 3/4 (first constants/window))
          mobs (get-in @state [:player :party])
          portrait (sprite/create-graphics (first constants/portrait-size) (second constants/portrait-size))]
      (loop [i 0]
        (let [portrait-x 50
              portrait-y (-> (* 10 i)
                             (+ (* (second constants/portrait-size) i))
                             (+ (* constants/padding i)))
              mob (get mobs i)]
          (q/with-graphics portrait
            (.clear portrait)
            (q/image (:portrait mob) 0 0))
          (q/image portrait constants/padding (+ 20 portrait-y))
          (ui/draw-line portrait-x portrait-y 0 (:name mob))
          (ui/draw-line portrait-x portrait-y 1 (format "%d/%d HP" (:hp mob) (:max-hp mob)))
          (ui/draw-line portrait-x portrait-y 2 (format "%d/%d Mana" (:mana mob) (:max-mana mob))))
        (if (< i (dec (count mobs)))
          (recur (inc i))))
      (ui/draw-cursor x 0 cursor)
      (ui/draw-line x 0 0 "Items")
      (ui/draw-line x 0 1 "Equipment")
      (ui/draw-line x 0 2 "Magic")
      (ui/draw-line x 0 3 "Quests")
      (ui/draw-line x 0 4 "Save")
      (ui/draw-line x 0 5 "Quit")))
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

