(ns dactyl-keyboard.mykeyboard
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.util :refer :all]))

(def columns (range 0 6))
(def rows (range 0 5))

(def switchhole-width 14.4)
(def switchhole-notch-width 6)
(def switchhole-notch-height 1.5)
(def switchhole-notch-depth 2.75)
(def plate-thickness 4)
(def amoeba-king-support-radius 1.25)
(def amoeba-king-support-length 2)
(def amoeba-king-support-distance 19)
(def keycap-space-width 19)
(def keyhole-side-width (/ (- keycap-space-width switchhole-width) 2))

(def epsilon 0.01)

(defn add-epsilon [x] (+ x epsilon))
(defn half-of [x] (/ x 2))
(defn half-cylinder [r h]
  (->> (difference (cylinder r h) (translate [0 (- (half-of r)) 0] (cube (add-epsilon (* 2 r)) r (add-epsilon h))))
       (translate [0 0 (half-of h)])
       (with-fn 30)))


(def top-wall (->> (difference
                    (->> (cube keycap-space-width keyhole-side-width plate-thickness)
                         (translate [0 (half-of keyhole-side-width) (half-of plate-thickness)]))
                    (->> (cube switchhole-notch-width (add-epsilon switchhole-notch-height) (add-epsilon switchhole-notch-depth))
                         (translate [0 (half-of switchhole-notch-height) (half-of switchhole-notch-depth)])))
                    ; Restore the translation center.
                   (translate [0 (- (half-of keyhole-side-width)) 0])
                   (translate [0 (half-of (+ switchhole-width keyhole-side-width)) 0])))

(def left-wall (->> (cube keyhole-side-width keycap-space-width plate-thickness)
                    (translate [(- (half-of (+ switchhole-width keyhole-side-width))) 0 (half-of plate-thickness)])))

(def amoeba-king-support (->> (half-cylinder amoeba-king-support-radius amoeba-king-support-length)
                              (rotate (- (half-of pi)) [0 0 1])
                              (translate [(- (half-of amoeba-king-support-distance)) 0 (-  amoeba-king-support-length)])))

(def half-keyhole (->> (union top-wall left-wall)
                       (union amoeba-king-support)))

(def keyhole (union half-keyhole
                    (rotate pi [0 0 1] half-keyhole)))

(defn column-y-offsets [i]
  (if (< i 0)
    0
    ([0 2 4 2 0 0] i)))

(defn translation-vector [col row] [(* keycap-space-width col) (+ (* (- keycap-space-width) row) (column-y-offsets col)) 0])

(defn key-place-flat [col row shape]
  (translate (translation-vector col row) shape))

(def thumb-cluster (->> (union (->> keyhole (key-place-flat 0 0))
                               (->> keyhole (key-place-flat -1 0))
                               (->> keyhole (key-place-flat -2 0)))
                        (key-place-flat 0 4)))
(def key-holes
  (apply union
         (for [col columns
               row rows
               :when (not
                      (or
                       (and (= col 0) (= row 4))
                       (and (= col 1) (= row 4))
                       (and (= col 4) (= row 4))
                       (and (= col 5) (= row 4))))]
           (->> keyhole
                (key-place-flat col row)))))
(def right-half (union key-holes thumb-cluster))

(spit "things/mykeyboard.scad"
      (write-scad right-half))
