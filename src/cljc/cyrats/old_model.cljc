(ns cyrats.model
  (:require [clojure.set :as set]))

(defn ->food [])

;;; module model
(defn ->module
  [hp ap dp]
  ;; TODO check min/max values
  {:hp hp
   :ap ap
   :dp dp})

(defn merge-module-stats
  [modules]
  ;; TODO try reduce
  (apply (partial merge-with +) modules))

;;; rat model
(defn ->rat
  "Rat constructor"
  [owner modules]
  {:modules (set modules)
   :owner owner
   :backpack []})

(defn rat-stats
  [{modules :modules}]
  (merge-module-stats modules))

(defn rat-energy-required
  [rat]
  (apply + (vals (rat-stats rat))))

;; arena rat
(defn rat-is-alive?
  [{arena-hp :arena-hp}]
  (pos? arena-hp))

(defn rat-can-loot?
  [{backpack :backpack}]
  (> 3 (count backpack)))

(defn rat-is-complete?
  [{modules :modules}]
  (= 3 (count modules)))

(defn rat-can-fight?
  [rat]
  (and (rat-is-alive? rat) (rat-can-loot? rat) (rat-is-complete? rat)))

;;; player model
(defn ->player
  "Player constructor"
  [modules food energy bot?]
  {:modules (set modules)
   :food food
   :energy energy
   :rats #{}
   :bot? bot?})


(defn player-can-assemble-rat?
  [{player-modules :modules} rat-modules]
  (and
   (= 3 (count rat-modules))
   (set/subset? rat-modules player-modules)))

(defn player-assemble-rat
  [player modules]
    (assoc
     (update-in player [:rats] conj (->rat player modules))
     :modules (set/difference (:modules player) modules)))

(defn player-can-send-rat-to-arena?
  [player rat]
  (and
   (>= (:energy player) (rat-energy-required rat))
   (contains? (:rats player) rat)))

(defn player-send-rat-to-arena
  [player rat])

;;; arena
(defn prepare-rat-for-arena
  [rat]
  (assoc rat :arena-hp (:hp (merge-module-stats (:modules rat)))))

(defn ->arena
  [rats]
  {:rats (set (map prepare-rat-for-arena rats))
   :dead #{}})

(defn pairs-for-fight
  [{rats :rats}]
  (partition 2 (shuffle (filter rat-can-fight? rats))))

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

(defn- arena-tick
  [arena]
  (doseq [[attacker target] (pairs-for-fight arena)]
    (bite attacker target)))


(defn ->game-room
  [players hunger-speed charge-speed]
  {:players (set players)
   :hunger-speed hunger-speed
   :charge-speed charge-speed})

(defn ->tick
  [game]
  {:messages [],
   :game game})
