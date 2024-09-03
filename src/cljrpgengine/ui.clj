(ns cljrpgengine.ui
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.sprite :as sprite]
            [cljrpgengine.util :as util]
            [clojure.string :as str]
            [quil.core :as q]))

(def panel (atom nil))
(def ui-pack (atom nil))

(defn init!
  []
  (swap! panel (fn [_] (q/load-image "panel.png")))
  (swap! ui-pack (fn [_] (q/load-image "ui.png"))))

(defn draw-line
  ([x y line-number text font-color]
   (q/with-fill (font-color constants/colors)
     (q/text text (+ x 30) (+ y 30 (* constants/line-spacing line-number)))))
  ([x y line-number text]
   (draw-line x y line-number text :font-default)))

(defn draw-window
  [x y width height]
  (q/with-fill (:window constants/colors)
    (q/rect x y width height))
  (let [f 48
        h (/ f 2)
        q (/ h 2)
        g (sprite/create-graphics h h)]
    ; upper left
    (q/with-graphics g
      (.clear g)
      (q/image @panel 0 0))
    (q/image g x y)

    ; lower left
    (q/with-graphics g
      (.clear g)
      (q/image @panel 0 (- h)))
    (q/image g x (-> (- height h)
                     (+ y)))

    ; left
    (q/with-graphics g
      (.clear g)
      (q/image @panel 0 (- q)))
    (q/image g x (+ y h) h (- height f))

    ; top
    (q/with-graphics g
      (.clear g)
      (q/image @panel (- q) 0))
    (q/image g (+ x h) y (- width f) h)

    ; upper right
    (q/with-graphics g
      (.clear g)
      (q/image @panel (- h) 0))
    (q/image g (-> (- width h)
                   (+ x)) y)

    ; lower right
    (q/with-graphics g
      (.clear g)
      (q/image @panel (- h) (- h)))
    (q/image g (-> (- width h)
                   (+ x)) (-> (- height h)
                              (+ y)))

    ; right
    (q/with-graphics g
      (.clear g)
      (q/image @panel (- h) (- q)))
    (q/image g (-> (- width h)
                   (+ x)) (+ y h) h (- height f))

    ; bottom
    (q/with-graphics g
      (.clear g)
      (q/image @panel (- q) (- h)))
    (q/image g (+ x h) (-> (- height h)
                           (+ y)) (- width f) h)

    ; middle
    (q/with-graphics g
      (.clear g)
      (q/image @panel (- q) (- q)))
    (q/image g h h  (- width f) (- height f))))

(defn- string-break
  [message]
  (let [words (str/split message #" ")]
    (loop [w 0
           l 0
           text ""]
      (if (< w (count words))
        (recur
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           w
           (inc w))
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           0
           (+ 1 l (count (words w))))
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           (str text "\n")
           (str text (words w) " ")))
        text))))

(defn draw-cursor
  [x y line]
  (let [g (sprite/create-graphics 16 16)]
    (q/with-graphics g
      (.clear g)
      (q/image @ui-pack -342 -468))
    (q/image g (+ x 10) (+ y 17 (* constants/line-spacing line)))))

(defn get-menu-cursor
  [state menu]
  (:cursor (util/filter-first #(= (.menu-type (:menu %)) menu) (:menus @state))))

(defn dialog
  [message]
  (let [y (* (second constants/window) 2/3)
        text (string-break message)]
    (draw-window 0 y (first constants/window) (* (second constants/window) 1/3))
    (draw-line 0 y 0 text)))

(defn draw-menus
  [state]
  (dorun
   (for [m (:menus @state)]
     (cond
       (:open m)
       (.draw (:menu m))))))

(defn open-menu!
  [state menu]
  (dosync
   (alter state update :menus conj {:menu menu
                                    :open true
                                    :cursor 0})))

(defn close-menu!
  [state]
  (dosync (alter state update-in [:menus] pop)))

(defn last-menu-index
  [state]
  (dec (count (:menus @state))))

(defn move-cursor!
  [state key]
  (let [m (last-menu-index state)]
    (cond
      (= key :up)
      (dosync (alter state update-in [:menus m :cursor] dec))
      (= key :down)
      (dosync (alter state update-in [:menus m :cursor] inc))
      (and
       (< (:quantity-min @state) (:quantity @state))
       (= key :left))
      (dosync (alter state update :quantity dec))
      (and
       (> (:quantity-max @state) (:quantity @state))
       (= key :right))
      (dosync (alter state update :quantity inc)))
    (if (> 0 (get-in @state [:menus m :cursor]))
      (dosync (alter state assoc-in [:menus m :cursor] (dec (.cursor-length (get-in @state [:menus m :menu])))))
      (if (= (.cursor-length (get-in @state [:menus m :menu])) (get-in @state [:menus m :cursor]))
        (dosync (alter state assoc-in [:menus m :cursor] 0))))))

(defn is-menu-open?
  [state]
  (> (count (:menus @state)) 0))

(defn text-fixed-width
  [text spaces]
  (if (> (count text) spaces)
    (str (subs text 0 (- spaces 4)) "... ")
    (loop [t text]
      (if (> spaces (count t))
        (recur (str t " "))
        t))))

(defn reset-quantity!
  [state min max]
  (dosync
   (alter state assoc :quantity 1 :quantity-min min :quantity-max max)))
