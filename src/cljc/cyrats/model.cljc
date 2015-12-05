(ns cyrats.model)

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
  (apply (partial merge-with +) modules))

;;; rat model
(defn ->rat
  "Rat constructor"
  [owner modules]
  {:modules modules
   :owner owner
   :backpack []
   })

(defn prepare-rat-for-arena
  [rat]
  (assoc rat :arena-hp (:hp (merge-module-stats (:modules rat)))))

(defn rat-stats
  [{modules :modules}]
  (merge-module-stats modules))

(defn rat-energy-required
  [rat]
  (apply + (vals (rat-stats rat))))

(defn rat-is-alive?
  [rat]
  (pos? (:arena-hp rat)))

(defn rat-is-not-full?
  [rat]
  (>= 3 (count (:backpack rat))))

(defn rat-can-fight?
  [rat]
  (and (rat-is-alive? rat) (rat-is-not-full? rat)))


;;; player model

(defn ->player
  "Player constructor"
  [modules food energy bot?]
  {:modules modules
   :food food
   :energy energy
   :rats []
   :bot? bot?
   })

(defn player-is-alive?
  [player]
  (> 0 (:food player)))


;;; arena
(defn ->arena
  [rats]
  {:rats rats
   :dead []})

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
    (update target :arena-hp - dmg)
    ))

(defn- arena-tick
  [arena]
  (doseq [[attacker target] (pairs-for-fight arena)]
    (bite attacker target)))


;; (defn assemble-rat
;;   [modules]
;;   {:pre (count mo)}
  ;;   )
