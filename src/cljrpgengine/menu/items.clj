(ns cljrpgengine.menu.items
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def item-name-width 30)
(def padding-left 30)

(defn get-item-description
  [mob-items index]
  (as-> mob-items v
    (keys v)
    (nth v index)
    (get @item/items v)
    (:description v)))

(defn create
  []
  (let [window (ui/create-window 0 0 constants/screen-width constants/screen-height)
        i (atom 1)
        description (ui/create-label (get-item-description @player/items (dec @i))
                                     padding-left
                                     (ui/line-number window 13))]
    (.addActor window (ui/create-label
                       (str (ui/text-fixed-width "Item" item-name-width) "Quantity")
                       padding-left
                       (ui/line-number window 1)))
    (.addActor window description)
    (menu/create-menu
     :items
     window
     (mapv (fn [[item-key quantity]]
             (let [{:keys [name type]} (-> @item/items item-key)]
               (menu/create-option
                (ui/create-label (str (ui/text-fixed-width
                                       name
                                       item-name-width)
                                      quantity)
                                 padding-left
                                 (ui/line-number window (swap! i inc))
                                 (if (= :consumable type)
                                   (:default constants/font-colors)
                                   (:disabled constants/font-colors)))
                #(println "foo"))))
           @player/items)
     (fn [cursor]
       (.setText description
                 (get-item-description @player/items cursor))))))
