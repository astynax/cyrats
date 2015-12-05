(ns cyrats.arena
  (:require
   [cyrats.rat :as rat]))

(defn pairs-for-fight
  [{rats :rats}]
  (partition 2 (shuffle (filter rat/can-fight? rats))))

(defn ^:dynamic *randomize-damage*
  [dmg]
  (* dmg (+ 0.75 (rand 0.75))))

(defn calculate-damage
  [ap dp]
  (let [ap' (float ap)]
    (*randomize-damage*
        (if (zero? dp)
        ap'
        (/ ap' dp)))))

(defn bite
  [attacker target]
  (let [
        ap (:ap attacker)
        dp (:dp target)
        dmg (calculate-damage ap dp)]
    (update target :arena-hp - dmg)))
