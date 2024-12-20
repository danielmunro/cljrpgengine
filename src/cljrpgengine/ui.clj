(ns cljrpgengine.ui
  (:require [cljrpgengine.constants :as constants]
            [cljrpgengine.log :as log]
            [cljrpgengine.util :as util]
            [cljrpgengine.window :as window]
            [clojure.string :as str])
  (:import (java.awt Font)
           (java.io File)))

(def panel (atom nil))
(def ui-pack (atom nil))
(def main-font (atom nil))
(def menus (atom []))
(def quantity (atom nil))
(def min-quantity (atom nil))
(def max-quantity (atom nil))

(defn init!
  []
  (swap! panel (fn [_] (util/load-image (str constants/sprites-dir "panel.png"))))
  (swap! ui-pack (fn [_] (util/load-image (str constants/sprites-dir "ui.png"))))
  (swap! main-font (fn [_] (.deriveFont
                            (Font/createFont Font/TRUETYPE_FONT
                                             (File. (str "resources/" constants/font-family)))
                            (float constants/text-size)))))

(defn draw-line
  ([x y line-number text font-color]
   (.setColor @window/graphics (font-color constants/colors))
   (.setFont @window/graphics @main-font)
   (.drawString @window/graphics
                text
                (float (+ x constants/padding))
                (float (+ y constants/padding constants/text-size (* constants/line-spacing line-number)))))
  ([x y line-number text]
   (draw-line x y line-number text :font-default)))

(defn draw-window
  [x y width height]
  (let [g @window/graphics
        p @panel
        f 48
        h (/ f 2)
        q (/ h 2)]
    (.setColor g (:window constants/colors))
    (.fillRect g x y width height)

    ; upper left
    (.drawImage g p x y (+ x h) (+ y h) 0 0 h h nil)

    ; lower left
    (let [y (-> height
                (- h)
                (+ y))]
      (.drawImage g p x y (+ x h) (+ y h) 0 h h (+ h h) nil))

    ; left
    (let [y (+ y h)
          h2 (- height f)]
      (.drawImage g p x y (+ x h) (+ y h2) 0 q h (+ q h) nil))

    ; top
    (let [dx1 (+ x h)
          dx2 (-> dx1
                  (+ width)
                  (- f))]
      (.drawImage g p dx1 y dx2 (+ y h) q 0 (+ q h) h nil))

    ; upper right
    (let [dx1 (-> width
                  (- h)
                  (+ x))]
      (.drawImage g p dx1 y (+ dx1 h) (+ y h) h 0 f h nil))

    ; lower right
    (let [dx1 (-> width
                  (- h)
                  (+ x))
          dy1 (-> height
                  (- h)
                  (+ y))]
      (.drawImage g p dx1 dy1 (+ dx1 h) (+ dy1 h) h h f f nil))

    ; right
    (let [dx1 (-> width
                  (- h)
                  (+ x))
          dy1 (+ y h)
          dy2 (-> dy1
                  (+ height)
                  (- f))]
      (.drawImage g p dx1 dy1 (+ dx1 h) dy2 h q f (+ q h) nil))

    ; bottom
    (let [dx1 (+ x h)
          dy1 (-> height
                  (- h)
                  (+ y))
          dx2 (-> dx1
                  (+ width)
                  (- f))]
      (.drawImage g p dx1 dy1 dx2 (+ dy1 h) q h (+ q h) f nil))))

(defn- string-break
  [message]
  (let [words (str/split message #" ")]
    (loop [w 0
           l 0
           lines []
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
           (conj lines text)
           lines)
         (if (> (+ l (count (words w)) 1) constants/dialog-text-width)
           ""
           (str text (words w) " ")))
        (conj lines text)))))

(defn draw-cursor
  ([x y]
   (.drawImage @window/graphics @ui-pack (- (+ x constants/padding) 14) (+ y constants/padding) (+ x 8) (+ y 26) 342 468 358 484 nil))
  ([x y line]
   (draw-cursor x (+ y (* constants/line-spacing line)))))

(defn get-menu-cursor
  [menu]
  (or (:cursor (util/filter-first #(= (.menu-type (:menu %)) menu) @menus))
      0))

(defn dialog
  [mob message]
  (let [y (- constants/screen-height constants/dialog-height)
        text (string-break (str (:name mob) ": " message))]
    (draw-window 0 y constants/screen-width constants/dialog-height)
    (doseq [i (range (count text))]
      (draw-line 0 y i (get text i)))))

(defn draw-menus
  []
  (doseq [menu @menus]
    (if (:open menu)
      (.draw (:menu menu)))))

(defn open-menu!
  [menu]
  (log/debug (format "opening menu :: %s" (name (.menu-type menu))))
  (swap! menus conj {:menu menu :open true :cursor 0}))

(defn close-menu!
  ([amount]
   (doseq [_ (range 0 amount)]
     (swap! menus pop)))
  ([]
   (close-menu! 1)))

(defn get-menu-index
  [menu]
  (loop [i 0]
    (if (= menu (.menu-type (:menu (@menus i))))
      i
      (if (< i (dec (count @menus)))
        (recur (inc i))))))

(defn get-last-menu
  []
  (.menu-type (:menu (last @menus))))

(defn last-menu-index
  []
  (dec (count @menus)))

(defn- cursor-can-move?
  []
  (> (.cursor-length (:menu (last @menus))) 0))

(defn change-cursor!
  ([f menu]
   (let [menu-index (get-menu-index menu)
         cursor-length (.cursor-length (get-in @menus [menu-index :menu]))
         cursor-path [menu-index :cursor]]
     (swap! menus update-in cursor-path f)
     (if (= cursor-length (get-in @menus cursor-path))
       (swap! menus assoc-in cursor-path 0)
       (if (< (get-in @menus cursor-path) 0)
         (swap! menus assoc-in cursor-path (dec cursor-length))))))
  ([f]
   (change-cursor! f (.menu-type (:menu (last @menus))))))

(defn- dec-cursor!
  []
  (change-cursor! dec))

(defn inc-cursor!
  []
  (change-cursor! inc))

(defn- above-min-quantity?
  []
  (if (and @min-quantity @quantity)
    (< @min-quantity @quantity)))

(defn- below-max-quantity?
  []
  (if (and @max-quantity @quantity)
    (< @quantity @max-quantity)))

(defn- inc-quantity!
  []
  (swap! quantity inc))

(defn- dec-quantity!
  []
  (swap! quantity dec))

(defn move-cursor!
  [key]
  (cond
    (and
     (= key :up)
     (cursor-can-move?))
    (dec-cursor!)
    (and
     (= key :down)
     (cursor-can-move?))
    (inc-cursor!)
    (and
     (= key :left)
     (above-min-quantity?))
    (dec-quantity!)
    (and
     (= key :right)
     (below-max-quantity?))
    (inc-quantity!)))

(defn is-menu-open?
  []
  (> (count @menus) 0))

(defn text-fixed-width
  [text spaces]
  (let [text-str (str text)]
    (if (> (count text-str) spaces)
      (str (subs text-str 0 (- spaces 4)) "... ")
      (loop [t text-str]
        (if (> spaces (count t))
          (recur (str t " "))
          t)))))

(defn reset-quantity!
  [min max]
  (swap! quantity (constantly 1))
  (swap! min-quantity (constantly min))
  (swap! max-quantity (constantly max)))

(defn scrollable-area
  [x y cursor max-lines-on-screen start-line lines]
  (let [offset (max 0 (- cursor max-lines-on-screen))
        line-count (count lines)]
    (draw-cursor x y (- (inc cursor) offset))
    (loop [i 0]
      (if (< i line-count)
        (let [line (get lines i)
              line-number (+ start-line i)]
          (if (< offset line-number)
            (line (- line-number offset)))
          (recur (inc i)))))))

(defn draw-portraits
  ([party item selected-mob]
   (doseq [i (range 0 (count party))]
     (let [identifier (nth (keys party) i)
           portrait-x 50
           portrait-y (-> (* 10 i)
                          (+ (* (second constants/portrait-size) i))
                          (+ (* constants/padding i)))
           {:keys [affect amount]} item
           {{:keys [hp max-hp mana max-mana portrait name xp level]} identifier} party
           amount-hp (if (= :restore-hp affect) (util/restore-amount amount hp max-hp))
           amount-mana (if (= :restore-mana affect) (util/restore-amount amount mana max-mana))]
       (.drawImage @window/graphics (:image portrait) constants/padding (+ 20 portrait-y) nil)
       (draw-line portrait-x portrait-y 0 name)
       (draw-line portrait-x
                  portrait-y
                  1
                  (str (format "%d/%d HP" hp max-hp)
                       (if (and (= i selected-mob)
                                (= :restore-hp affect))
                         (format " +%d" amount-hp))))
       (draw-line
        portrait-x
        portrait-y
        2
        (str (format "%d/%d Mana" mana max-mana)
             (if (and (= i selected-mob)
                      (= :restore-mana affect))
               (format " +%d" amount-mana))))
       (draw-line
        (+ portrait-x 200)
        portrait-y
        1
        (format "level %d" level))
       (draw-line
        (+ portrait-x 200)
        portrait-y
        2
        (format "%d xp to level" (util/get-xp-to-level xp))))))
  ([party]
   (draw-portraits party nil -1)))
