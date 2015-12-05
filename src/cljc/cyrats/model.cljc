(ns cyrats.model)

(defn ->food [])


(defn ->module
  [hp ap dp]
  ;; TODO check min/max values
  {:hp hp
   :ap ap
   :dp dp})

(defn modules-total-stat
  [modules stat]
  (apply + (map stat modules)))


(defn ->rat
  "Rat counstructor"
  [modules]
  {:modules modules
   :backpack []
   })

(defn rat-stats
  [{modules :modules} rat]
  {:hp (modules-total-stat modules :hp)
   :ap (modules-total-stat modules :ap)
   :dp (modules-total-stat modules :dp)})

(defn rat-can-fight?
  [rat]
  (or (>= 3 (count (:backpack rat)))
      (> 0 (modules-total-stat :hp)))
)



(defn ->player
  "Player constructor"
  [modules food energy]
  {:modules modules
   :food food
   :energy energy
   :rats []
   })

(defn player-is-alive?
  [player]
  (> 0 (:food player)))




;; (defn assemble-rat
;;   [modules]
;;   {:pre (count mo)}
;;   )

(defn )

