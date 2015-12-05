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
  [module1 module2 module3]
  {:modules (vec (list module1 module2 module3))
   :backpack []})

(defn rat-stats
  [{modules :modules}]
  (merge-module-stats modules))

(defn rat-energy-required
  [rat]
  (apply + (vals (rat-stats rat))))

(defn rat-is-alive?
  [rat]
  (> 0 (:hp (rat-stats rat))))

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


;; (defn assemble-rat
;;   [modules]
;;   {:pre (count mo)}
;;   )
