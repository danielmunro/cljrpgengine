(ns cljrpgengine.menu.use-item
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.select-party :as select-party-menu]
            [cljrpgengine.ui :as ui]))

(defn- create-apply-item
  [item-key mob-key]
  (menu/create-menu-2
    :apply-item
    (ui/create-window 100 100 100 100)
    nil
    []
    #()
    (ui/create-cursor)))

(defn- create-label-for-item
  [window index {:keys [affect amount]}]
  (cond
    (= :restore-hp affect)
    (ui/create-label (str "+" amount)
                     200
                     (- (ui/line-number window 2)
                        (* 80 index))
                     (:success constants/font-colors))
    (= :restore-mana affect)
    (ui/create-label (str "+" amount)
                     200
                     (- (ui/line-number window 3)
                        (* 80 index))
                     (:success constants/font-colors))))

(defn create
  [item-key]
  (let [{:keys [window] :as menu} (select-party-menu/create (partial create-apply-item item-key))
        label (atom (create-label-for-item window @(-> menu :cursor :index) (get @item/items item-key)))]
    (.addActor window @label)
    (assoc
      menu
      :on-change (fn [_]
                   (.removeActor window @label)
                   (.addActor window (swap! label (constantly
                                  (create-label-for-item window
                                                         @(-> menu :cursor :index)
                                                         (get @item/items item-key)))))))))
