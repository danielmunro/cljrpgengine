(ns cljrpgengine.menu)

(defprotocol Menu
  (draw [menu])
  (cursor-length [menu])
  (menu-type [menu])
  (key-pressed [menu]))
