(ns mykeyboard.utils
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mykeyboard.globals :refer :all]))

(defn add-epsilon [x] (+ x epsilon))
(defn half-of [x] (/ x 2))
(defn half-cylinder [r h]
  (->> (difference (cylinder r h)
                   (->> (cube (* 2 r) r h)
                        (translate [(- r) 0 0])))
       (rotate (/ pi 2) [0 0 1])
       (with-fn 60)))