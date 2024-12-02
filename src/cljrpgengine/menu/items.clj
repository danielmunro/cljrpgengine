(ns cljrpgengine.menu.items
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.menu :as menu]
            [cljrpgengine.ui :as ui]))

(defn create
  []
  (menu/create-menu
   :items
   (menu/create-window 0 0 constants/screen-width constants/screen-height)
   [#_(menu/create-option (ui/create-label "test" 0 0) #())]))
