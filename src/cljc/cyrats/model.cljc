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
  {:modules modules
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
  {:modules modules
   :food food
   :energy energy
   :rats []
   :bot? bot?})

(defn player-is-alive?
  [{food :food}]
  (pos? food))

(defn player-can-assemble-rat?
  [{player-modules :modules} rat-modules]
  (and
   (= 3 (count rat-modules))
   (set/subset? (set rat-modules) (set player-modules))))

(defn player-assemble-rat
  [player modules]
  (if (player-can-assemble-rat? player modules)
    (update-in player [:rats] (->rat player modules))
    (update player :modules )))

(defn player-can-send-rat-to-arena?
  [player rat]
  )

(defn player-send-rat-to-arena
  [player rat]
  )

;;; arena
(defn ->arena
  [rats]
  {:rats rats
   :dead []})

(defn pairs-for-fight
  [{rats :rats}]
  (partition 2 (shuffle (filter rat-can-fight? rats))))

(defn prepare-rat-for-arena
  [rat]
  (assoc rat :arena-hp (:hp (merge-module-stats (:modules rat)))))

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


;; (defn assemble-rat
;;   [modules]
;;   {:pre (count mo)}
  ;;   )
