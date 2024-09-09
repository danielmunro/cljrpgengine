(ns cljrpgengine.class)

(defn hp-for-level
  [class]
  (cond
    (= class :warrior)
    (-> (* (rand-int 8))
        (+ 15))
    (= class :mage)
    (-> (* (rand-int 4))
        (+ 5))
    (= class :rogue)
    (-> (* (rand-int 6))
        (+ 9))
    (= class :cleric)
    (-> (* (rand-int 5))
        (+ 8))
    (= class :none)
       0))

(defn mana-for-level
  [class]
  (cond
    (= class :warrior)
    (-> (* (rand-int 3))
        (+ 5))
    (= class :mage)
    (-> (* (rand-int 12))
        (+ 15))
    (= class :rogue)
    (-> (* (rand-int 6))
        (+ 7))
    (= class :cleric)
    (-> (* (rand-int 10))
        (+ 12))
    (= class :none)
       0))
