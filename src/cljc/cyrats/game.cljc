(ns cyrats.game
  (:require
   [clojure.math.combinatorics :refer [selections]]
   [cyrats.utils :as utils]
   [cyrats.rat :as rat]))


(defn make-modules-for-game []
  (map #(apply ->module %) (shuffle (selections [0 1 2 3 4] 3))))


(defn ->game [players]
  {:players (map #(->player % 10 20) players)}
  :messages [])





;; (defn give-init-modules [players modules cnt]
;;   (map #(update % :modules (take cnt modules))))
