(ns dactyl-keyboard.mykeyboard
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.util :refer :all]))

(def columns (range 0 6))
(def rows (range 0 5))

(def switchhole-width 14.1)
(def switchhole-notch-width 6)
(def switchhole-notch-height 1.5)
(def switchhole-notch-depth 2.75)
(def plate-thickness 4)
(def amoeba-king-support-radius 1.25)
(def amoeba-king-support-distance 19)
(def keycap-space-width 19)
(def keyhole-side-width (/ (- keycap-space-width switchhole-width) 2))

(def top-wall (->> (cube keycap-space-width keyhole-side-width plate-thickness)
                   (translate [0 (/ switchhole-width 2) 0])))
(def left-wall (->> (cube keyhole-side-width keycap-space-width plate-thickness)
                    (translate [(- (/ switchhole-width 2)) 0 0])))

(def keyhole (union top-wall left-wall))

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
