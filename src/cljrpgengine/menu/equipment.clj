(ns cljrpgengine.menu.equipment
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.item :as item]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.menu.common :as common]
            [cljrpgengine.menu.select_equipment :as select-equipment-menu]
            [cljrpgengine.mob :as mob]
            [cljrpgengine.player :as player]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.util :as util])
  (:import (com.badlogic.gdx.scenes.scene2d Group)))

(def equipment-name-length 15)

(defn- get-equipment-name
  [equipment-name]
  (if equipment-name
    (if (< equipment-name-length (count equipment-name))
      (str (subs equipment-name 0 equipment-name-length) "...")
      equipment-name)
    "none"))

(defn create
  [mob-key]
  (let [{portrait :portrait mob-name :name} (get @player/party mob-key)
        window (ui/create-window
                0
                0
                constants/screen-width
                constants/screen-height)
        i (atom 2)
        column-1-padding (+ (.getWidth portrait)
                            constants/padding
                            constants/left-cursor-padding)
        left-pane (Group.)
        right-pane (doto (Group.)
                     (.setX (* constants/screen-width 2/3)))
        mob (get @player/party mob-key)
        options (mapv (fn [equipment-position]
                        (assoc (menu/create-option
                                (ui/create-label (str (ui/text-fixed-width (name equipment-position) 15)
                                                      (get-equipment-name (:name (get @item/items (get @(:equipment mob) equipment-position)))))
                                                 column-1-padding
                                                 (ui/line-number window (swap! i inc)))
                                #(menu/add-menu! (select-equipment-menu/create mob-key equipment-position)))
                               :equipment-position equipment-position))
                      (keys @(:equipment mob)))]
    (.addActor window (util/create-image portrait
                                         constants/padding
                                         (- constants/screen-height
                                            (.getHeight portrait)
                                            constants/padding)))
    (.addActor left-pane (ui/create-label (str "Equipment for " mob-name ": ")
                                          column-1-padding
                                          (ui/line-number window 1)))
    (common/draw-attributes window right-pane mob)
    (doseq [o options]
      (.addActor left-pane (:label o)))
    (.addActor window left-pane)
    (.addActor window right-pane)
    (menu/create-menu
     :equipment
     window
     {:add-to-window false
      :options options}
     (fn [event]
       (when (= (:event-type event) :updated)
         (common/draw-attributes window right-pane mob)
         (let [equipment-position (-> event :changed :equipment-position)
               option (first (filter #(= equipment-position (:equipment-position %))
                                     (-> @menu/opened-menus
                                         (last)
                                         :options)))]
           (.setText (:label option)
                     (str (ui/text-fixed-width (name equipment-position) 15)
                          (get-equipment-name (-> event :changed :equipment :name))))))))))
