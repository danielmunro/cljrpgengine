(ns cljrpgengine.menu.items
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.deps :as deps]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.use-item :as use-item-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]))

(def item-name-width 30)
(def padding (* constants/padding 3))

(defn get-item-description
  [mob-items index]
  (as-> mob-items v
    (keys v)
    (nth v index)
    (get @item/items v)
    (:description v)))

(defn create
  []
  (let [window (ui/create-window 0
                                 0
                                 constants/screen-width
                                 constants/screen-height)
        i (atom 0)
        description (ui/create-label (get-item-description @player/items @i)
                                     padding
                                     (ui/line-number window 18))
        item-count (count @player/items)
        height (max (- (.getHeight window)
                       (* (.getLineHeight @deps/font) 2))
                    (* item-count (.getLineHeight @deps/font)))
        options (mapv (fn [[item-key quantity]]
                        (let [{:keys [name type]} (-> @item/items item-key)]
                          (menu/create-option
                           (ui/create-label (str (ui/text-fixed-width
                                                  name
                                                  item-name-width)
                                                 quantity)
                                            padding
                                            (- height (* (swap! i inc) (.getLineHeight @deps/font)))
                                            (if (= :consumable type)
                                              (:default constants/font-colors)
                                              (:disabled constants/font-colors)))
                           (fn []
                             (if (= :consumable type)
                               (menu/add-menu! (use-item-menu/create item-key)))))))
                      @player/items)
        {:keys [cursor option-group]} (menu/create-option-group options (.getWidth window))
        scroll-pane (menu/scrollable option-group
                                     0
                                     (.getLineHeight @deps/font)
                                     (.getWidth window)
                                     (- (.getHeight window)
                                        (* (.getLineHeight @deps/font) 2)))]
    (.addActor window (ui/create-label
                       (str (ui/text-fixed-width "Item" item-name-width) "Quantity")
                       padding
                       (ui/line-number window 1)))
    (.addActor window description)
    (.addActor window scroll-pane)
    (menu/create-menu-2
     :items
     window
     scroll-pane
     options
     cursor
     (fn [event]
       (.setText description
                 (get-item-description @player/items (:changed event)))))))
