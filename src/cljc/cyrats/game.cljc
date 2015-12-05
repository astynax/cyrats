(ns cyrats.game
  (:require [clojure.math.combinatorics :refer [selections]]))

;;; utils
(defn log-message [game message]
  (update-in game [:messages] (fn [v] (conj v message))))


(defn clear-messages
  [game]
  (update-in game [:messages] (fn [v] (vec []))))

(defn merge-module-stats
  [modules]
  ;; TODO try reduce
  (apply (partial merge-with +) modules))

(defn ->module [hp ap dp]
  {:hp hp
   :ap ap
   :dp dp})

(defn ->player [name food energy]
  {:modules []
   :food food
   :name name
   :energy energy})

(defn player-tick [player]
  (-> player
   (update :food - 10)
   (update :energy + 10)))


(defn make-modules-for-game []
  (map #(apply ->module %) (shuffle (selections [0 1 2 3 4] 3))))


(defn ->game [players]
  {:players (map #(->player % 10 20) players)}
  :messages [])





;; (defn give-init-modules [players modules cnt]
;;   (map #(update % :modules (take cnt modules))))
