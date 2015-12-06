(ns cyrats.domain.game
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.arena :as arena]
            [cyrats.domain.rat :as rat]))

;; utils
(defn ->module
  [hp ap dp]
  {:pre [(<= 0 hp 4)
         (<= 0 ap 4)
         (<= 0 dp 4)]}
  {:hp hp
   :ap ap
   :dp dp})

(defn ->player
  [modules]
  {:modules (set modules)
   :rats []
   :food (*DEFAULTS* :initial-food)
   :energy (*DEFAULTS* :initial-energy)})

(defn ->session
  [log-fn]
  {:turn 0
   :log-fn log-fn
   :modules (map (partial apply ->module) (shuffle *MODULES*))
   :players {}
   :deads []
   :arena (arena/->arena log-fn)
   })

(defn log-message [session message]
  (update session :messages conj message))

(defn add-player
  [{:keys [modules players] :as session}
   user-id]
  (if (contains? players user-id)
    session ;; adding of existing player changes nothing
    (let [[starter-pack rest-modules] (split-at 5 modules)]
      (-> session
          (update :players assoc user-id (->player starter-pack))
          (assoc :modules rest-modules)))))

(defn- process-resources
  "Applies food consumption and producing of energy to the each of players"
  [session]
  (let [food-portion (*DEFAULTS* :starving-rate)
        energy-portion (*DEFAULTS* :energize-rate)]
    (reduce (fn [s id]
              (update-in s [:players id]
                         (fn [p]
                           (-> p
                               (update :food
                                       (fn [x]
                                         (max 0 (- x food-portion))))
                               (update :energy + energy-portion)))))
            session
            (keys (:players session)))))

(defn- collect-deads
  [{:keys [log-fn players] :as session}]
  (let [deads (filter (fn [[_ p]] (zero? (p :food)))
                      players)]
    (reduce (fn [s [id p]]
              (log-fn [:player-dead p])
              (-> s
                  (update :players dissoc id)
                  (update :deads conj p)))
            session
            deads)))

(defn populate
  [session]
  (-> session
      process-resources
      collect-deads
      ))
