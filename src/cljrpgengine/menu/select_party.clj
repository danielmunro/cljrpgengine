(ns cljrpgengine.menu.select-party
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.common :as common]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(defn create
  [next-menu]
  (let [window (ui/create-window 0 0
                                 common/menu-width
                                 constants/screen-height)
        {:keys [images group]} (common/draw-portraits)
        i (atom -1)]
    (.addActor window group)
    (menu/create-menu-2
     :select-party
     window
     (mapv (fn [image]
             (let [index (swap! i inc)]
               (menu/create-option
                image
                #(menu/add-menu! (next-menu (nth (keys @player/party) index))))))
           images))))
