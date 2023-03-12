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

(def keyswitch-height 14.4) ;; Was 14.1, then 14.25
(def keyswitch-width 14.4)


(def alps-notch-width 15.5)
(def alps-notch-height 1)
(def alps-height 13)

;; (def single-plate
;;   (let [top-wall (->> (cube (+ keyswitch-width 3) 2.2 plate-thickness)
;;                       (translate [0
;;                                   (+ (/ 2.2 2) (/ alps-height 2))
;;                                   (/ plate-thickness 2)]))
;;         left-wall (union (->> (cube 1.5 (+ keyswitch-height 3) plate-thickness)
;;                               (translate [(+ (/ 1.5 2) (/ 15.6 2))
;;                                           0
;;                                           (/ plate-thickness 2)]))
;;                          (->> (cube 1.5 (+ keyswitch-height 3) 1.0)
;;                               (translate [(+ (/ 1.5 2) (/ alps-notch-width 2))
;;                                           0
;;                                           (- plate-thickness
;;                                              (/ alps-notch-height 2))])))
;;         plate-half (union top-wall left-wall)]
;;     (union plate-half
;;            (->> plate-half
;;                 (mirror [1 0 0])
;;                 (mirror [0 1 0])))))

(def sa-profile-key-height 12.7)
(def cap-top-height (+ plate-thickness sa-profile-key-height))

(defn key-place-flat [column row shape]
  (translate [(* 20 column) (* -20 row) 0] shape))

;; (def key-holes
;;   (apply union
;;          (for [column columns
;;                row rows
;;                :when (or (not= column 0)
;;                          (not= row 4))]
;;            (->> single-plate
;;                 (key-place-flat column row)))))

(spit "things/mykeyboard.scad"
      (write-scad keyhole))
