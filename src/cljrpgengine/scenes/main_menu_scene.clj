(ns cljrpgengine.scenes.main-menu-scene
  (:require [cljrpgengine.scene :as scene]
            [cljrpgengine.ui :as ui]
            [cljrpgengine.menus.main-menu :as main-menu]))

(deftype MainMenuScene [state]
  scene/Scene
  (initialize-scene [_] (ui/open-menu! state (main-menu/create-menu state)))
  (update-scene [_])
  (scene-name [_] :main-menu)
  (shops [_] {}))

(defn create
  [state]
  (MainMenuScene. state))
