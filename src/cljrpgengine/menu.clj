(ns cljrpgengine.menu
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.ui :as ui]
            [quil.core :as q]))

(defprotocol Menu
  (draw [menu state])
  (cursor-length [menu state])
  (menu-type [menu]))

(deftype PartyMenu []
  Menu
  (draw [menu state] (ui/draw-window 0 0 (first constants/window) (second constants/window))
    (let [g (sprite/create-graphics 16 16)
          x (* 3/4 (constants/window 0))]
      (q/with-graphics g
                       (.clear g)
                       (q/image @ui/ui-pack -342 -468))
      (q/image g (- x 20) (-> (get-in @state [:menus (- (count (:menus @state)) 1) :cursor])
                              (* 20)
                              (+ 5)))
      {:menus #{:party-menu {:open true, :cursor 0}}}
      (q/with-fill [255 255 255]
                   (q/text "Items" x 20)
                   (q/text "Magic" x 40)
                   (q/text "Quests" x 60)
                   (q/text "Save" x 80)
                   (q/text "Exit" x 100))))
  (cursor-length [_ _] 5)
  (menu-type [_] :party))

(defn create-party-menu
  []
  (PartyMenu.))
