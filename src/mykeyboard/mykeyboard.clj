(ns mykeyboard.mykeyboard
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [mykeyboard.globals :refer :all]
            [mykeyboard.utils :refer :all]))

(binding [*center* false]

  (def switchhole-notch
    (cube switchhole-notch-width (add-epsilon switchhole-notch-height) (add-epsilon switchhole-notch-depth)))

  (spit "things/switchhole-notch.scad" (write-scad switchhole-notch))

  (def top-wall (->> (difference
                      (->> (cube keycap-space-width keyhole-side-width plate-thickness)
                           (translate [0 0 0]))
                      (->> switchhole-notch
                           (translate [(half-of (- keycap-space-width switchhole-notch-width)) (- keyhole-side-width switchhole-notch-height) 0])))))

  (spit "things/top-wall.scad" (write-scad top-wall))

  (def left-wall (cube keyhole-side-width keycap-space-width plate-thickness))

  (spit "things/left-wall.scad" (write-scad left-wall))

  (spit "things/half-cylinder.scad" (write-scad (half-cylinder amoeba-king-support-radius amoeba-king-support-length)))

  (def amoeba-king-support (->> (half-cylinder amoeba-king-support-radius amoeba-king-support-length)
                                (translate [0 (half-of keycap-space-width) (-  amoeba-king-support-length)])))

  (def half-keyhole (->> (union top-wall left-wall)
                         (union amoeba-king-support)))

  (spit "things/half-keyhole.scad" (write-scad half-keyhole))

  (def keyhole (union half-keyhole
                      (->> half-keyhole
                           (translate [(- (half-of keycap-space-width))
                                       (- (half-of keycap-space-width))
                                       0])
                           (rotate pi [0 0 1])
                           (translate [(half-of keycap-space-width)
                                       (half-of keycap-space-width)
                                       0]))))

  ;; Four poles that delimit the corners of the keyhole. Used in filler generation.
  (def top-left-pole (->> (cylinder epsilon plate-thickness)
                          (translate [0 keycap-space-width 0])))
  (def top-right-pole (->> (cylinder epsilon plate-thickness)
                           (translate [keycap-space-width keycap-space-width 0])))
  (def bottom-left-pole (cylinder epsilon plate-thickness))
  (def bottom-right-pole (->> (cylinder epsilon plate-thickness)
                              (translate [keycap-space-width 0 0])))

  (spit "things/keyhole.scad" (write-scad keyhole))

  (defn column-y-offsets [i]
    (if (< i 0)
      0
      ([0 2 4 2 0 0] i)))

  (defn translation-vector [col row] [(* keycap-space-width col) (+ (* (- keycap-space-width) row) (column-y-offsets col)) 0])

  (defn key-place-flat [col row shape]
    (translate (translation-vector col row) shape))

  (def rotation-rad (* 0.10 pi))

  (def rotation-radius (/ (half-of keycap-space-width) (Math/tan (/ rotation-rad 2))))

  (defn thumb-key-place
    "Places the num-th key in the thumb cluster to its designated position."
    [num block]
    (let
     [center (fn [block] (translate [(- (half-of keycap-space-width)) 0 0] block))
      turn (fn [rad, block] (rotate rad [0 0 1] block))
      fanout (fn [block] (translate [0 rotation-radius 0] block))]
      (->> block
           center
           fanout
           (turn (* num rotation-rad)))))

  (def thumb-cluster
    (let [fanin (fn [block] (translate [0 (- rotation-radius) 0] block))
          thumb-keyholes (union (->> keyhole
                                     (thumb-key-place 0))
                                (->> keyhole
                                     (thumb-key-place 1))
                                (->> keyhole
                                     (thumb-key-place 2)))]
      (->> thumb-keyholes
           (fanin)
           (translate [(half-of keycap-space-width) 0 0]))))

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

  (def right-half
    (let [left-padding 10
          thumb-cluster-nudge 5]
      (->> (union key-holes
                  (->>
                   thumb-cluster
                   (key-place-flat 0 4)
                   (translate [thumb-cluster-nudge 0 0])))
           (translate [0 (- keycap-space-width) 0])
           (rotate (* 0.15 pi) [0 0 1])
           (translate [left-padding 0 0]))))

  (spit "things/right-half.scad"
        (write-scad right-half))

  (def left-half (->> right-half
                      (mirror [1 0 0])))


  (spit "things/mykeyboard.scad"
        (write-scad (union left-half right-half))))