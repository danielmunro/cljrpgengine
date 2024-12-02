(ns cljrpgengine.input-adapter
  (:import (com.badlogic.gdx Input$Keys InputAdapter)))

(defn dungeon-input-adapter
  [key-down! key-up! key-typed!]
  (proxy [InputAdapter] []
    (keyDown [key]
      (cond
        (= key Input$Keys/LEFT)
        (key-down! :left)
        (= key Input$Keys/RIGHT)
        (key-down! :right)
        (= key Input$Keys/UP)
        (key-down! :up)
        (= key Input$Keys/DOWN)
        (key-down! :down)
        :else false))
    (keyUp [key]
      (cond
        (= key Input$Keys/LEFT)
        (key-up! :left)
        (= key Input$Keys/RIGHT)
        (key-up! :right)
        (= key Input$Keys/UP)
        (key-up! :up)
        (= key Input$Keys/DOWN)
        (key-up! :down)
        :else false))
    (keyTyped [key]
      (cond
        (= (str key) "c")
        (key-typed! :c)
        (= (str key) "m")
        (key-typed! :m)
        (= (str key) "q")
        (key-typed! :q)
        (= key \uF700)
        (key-typed! :up)
        (= key \uF701)
        (key-typed! :down)
        (= key \uF702)
        (key-typed! :left)
        (= key \uF703)
        (key-typed! :right)
        :else false))))
