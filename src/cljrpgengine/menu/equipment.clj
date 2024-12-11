(ns cljrpgengine.menu.equipment
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.select_equipment :as select-equipment-menu]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util]))

(defn create
  [mob-key]
  (let [{portrait :portrait mob-name :name} (get @player/party mob-key)
        window (ui/create-window
                0
                0
                constants/screen-width
                constants/screen-height)
        i (atom 2)
        x-padding (+ (.getWidth portrait)
                     constants/padding
                     constants/left-cursor-padding)
        mob (get @player/party mob-key)]
    (.addActor window (util/create-image portrait
                                         constants/padding
                                         (- constants/screen-height
                                            (.getHeight portrait)
                                            constants/padding)))
    (.addActor window (ui/create-label (str "Equipment for " mob-name ": ")
                                       x-padding
                                       (ui/line-number window 1)))
    (menu/create-menu
     :equipment
     window
     (mapv (fn [equipment-position]
             (assoc (menu/create-option
                     (ui/create-label (str (ui/text-fixed-width (name equipment-position) 15)
                                           (if-let [equipment (get @(:equipment mob) equipment-position)]
                                             (:name (get @item/items equipment))
                                             "none"))
                                      x-padding
                                      (ui/line-number window (swap! i inc)))
                     #(menu/add-menu! (select-equipment-menu/create mob-key equipment-position)))
                    :equipment-position equipment-position))
           (keys @(:equipment mob)))
     (fn [event]
       (when (= (:event-type event) :updated)
         (let [equipment-position (-> event :changed :equipment-position)
               option (first (filter #(= equipment-position (:equipment-position %))
                                     (-> @menu/opened-menus
                                         (last)
                                         :options)))]
           (.setText (:label option)
                     (str (ui/text-fixed-width (name equipment-position) 15)
                          (if-let [equipment (-> event :changed :equipment :name)]
                            equipment
                            "none")))))))))
