(ns cljrpgengine.menu.items
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(def item-name-width 30)
(def padding-left 30)

(defn create
  [mob]
  (let [window (ui/create-window 0 0 constants/screen-width constants/screen-height)
        i (atom 1)]
    (.addActor window (ui/create-label
                       (str (ui/text-fixed-width "Item" item-name-width) "Quantity")
                       padding-left
                       (ui/line-number window 1)))
    (menu/create-menu
     :items
     window
     (mapv (fn [[item-key quantity]]
             (menu/create-option
              (ui/create-label (str (ui/text-fixed-width
                                     (-> @item/items item-key :name)
                                     item-name-width)
                                    quantity)
                               padding-left
                               (ui/line-number window (swap! i inc)))
              #(println "foo")))
           @(:items mob)))))
