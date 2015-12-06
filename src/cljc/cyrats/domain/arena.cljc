(ns cyrats.domain.arena
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.rat :as rat]))

(defn ->arena
  []
  {:id-cnt 0
   :rats {}})

(defn add-rat
  [{:keys [id-cnt] :as arena} rat]
  (-> arena
      (update :rats assoc id-cnt rat)
      (assoc :id-cnt (inc id-cnt))))

;; rats confrontation

(defn- ^:dynamic *randomize-damage*
  [dmg]
  (* dmg (+ 0.75 (rand 0.75))))

(defn bite
  [attacker target]
  (let [ap (:attack attacker)
        dp (:defence target)]
    (if (zero? ap)
      0
      (let [ap' (float ap)]
        (*randomize-damage*
         (if (zero? dp)
           ap'
           (/ ap' dp)))))))

(defn confront
  [rat1 rat2]
  (if (= (:user-id rat1)
         (:user-id rat2))
    [rat1 rat2]
    (let [dmg2 (bite rat1 rat2)
          dmg1 (bite rat2 rat1)]
      [(rat/damage rat1 dmg1)
       (rat/damage rat2 dmg2)])))
