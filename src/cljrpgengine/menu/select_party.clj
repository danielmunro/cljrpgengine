(ns cljrpgengine.menu.select-party
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.common :as common]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(defn create
  [next-menu]
  (let [window (ui/create-window 0 0
                                 (- constants/screen-width 150)
                                 constants/screen-height)
        images (common/draw-portraits window)
        i (atom -1)]
    (menu/create-menu
     :select-party
     window
     (mapv (fn [image]
             (let [index (swap! i inc)]
               (menu/create-option
                image
                #(menu/add-menu! (next-menu (nth (keys @player/party) index))))))
           images))))
