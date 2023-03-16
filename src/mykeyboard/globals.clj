(ns mykeyboard.globals)

(def columns (range 0 6))
(def rows (range 0 5))

(def switchhole-width 14.4)
(def switchhole-notch-width 6)
(def switchhole-notch-height 1.5)
(def switchhole-notch-depth 2.75)
(def plate-thickness 4)
(def amoeba-king-support-radius 1.25)
(def amoeba-king-support-length 2)
(def keycap-space-width 19)
(def keyhole-side-width (/ (- keycap-space-width switchhole-width) 2))

(def epsilon 0.01)