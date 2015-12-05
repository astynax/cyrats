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
  "Rat counstructor"
  [module1 module2 module3]
  {:modules (vec (list module1 module2 module3))
   :backpack []})

(defn rat-stats
  [{modules :modules}]
  (merge-module-stats modules))

(defn rat-can-fight?
  [rat]
  (or (>= 3 (count (:backpack rat)))
      (> 0 (:hp (rat-stats rat)))))



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




;; (defn assemble-rat
;;   [modules]
;;   {:pre (count mo)}
;;   )
