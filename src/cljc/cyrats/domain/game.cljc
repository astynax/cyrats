(ns cyrats.domain.game
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.arena :as arena]
            [cyrats.domain.rat :as rat]))

;; utils
(defn log-message [session message]
  (update session :messages conj message))

(defn ->module
  [hp ap dp]
  ;; TODO check min/max values
  {:hp hp
   :ap ap
   :dp dp})

;; arena rat
;; (defn rat-is-alive?
;;   [{arena-hp :arena-hp}]
;;   (pos? arena-hp))

;; (defn rat-can-loot?
;;   [{backpack :backpack}]
;;   (> 3 (count backpack)))

;; (defn rat-is-complete?
;;   [{modules :modules}]
;;   (= 3 (count modules)))

;; (defn rat-can-fight?
;;   [rat]
;;   (and (rat-is-alive? rat) (rat-can-loot? rat) (rat-is-complete? rat)))

;; (defn rat-add-item
;;   [rat item]
;;   (if (rat-is-complete? rat)
;;     rat
;;     (update rat :backpack conj item)))

;; (defn rat-prepare-for-arena
;;   [rat]
;;   (assoc rat :arena-hp (:hp (merge-module-stats (:modules rat)))))

;; ;;; player

;; (defn ->player [modules food energy]
;;   {:modules (set modules)
;;    :food food
;;    :name "foo"
;;    :energy energy})

;; (defn player-tick [player]
;;   (-> player
;;       (update :food - 10)
;;       (update :energy + 10)))

;; (defn player-is-alive? [{food :food}]
;;   (pos? food))

;; (defn player-can-assemble-rat?
;;   [{player-modules :modules} rat-modules]
;;   (and
;;    (= 3 (count rat-modules))
;;    (set/subset? rat-modules player-modules)))

;; (defn player-assemble-rat
;;   [player modules]
;;   (-> player
;;       (update-in [:rats] conj (->rat player modules))
;;       (assoc :modules (set/difference (:modules player) modules))))

;; (defn player-can-send-rat-to-arena?
;;   [player rat]
;;   (and
;;    (>= (:energy player) (rat-energy-required rat))
;;    (contains? (:rats player) rat)))

;; (defn player-send-rat-to-arena
;;   [player rat])


;; ;;; game
;; (defn ->game [players]
;;   (let [modules (partition 5 (make-modules-for-game))]
;;     {:player (map #(apply ->player % %2 10 20) (map vector players modules))
;;      :messages []}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ->player
  [modules]
  {:modules (set modules)
   :food (*DEFAULTS* :initial-food)
   :energy (*DEFAULTS* :initial-energy)})

(defn ->session
  []
  {:modules (map ->module (shuffle *MODULES*))
   :players {}
   :arena (arena/->arena)
   :log []
   :starving-rate (*DEFAULTS* :starving-rate)
   :energize-rate (*DEFAULTS* :energize-rate)})

(defn add-player
  [{:keys [modules players] :as session}
   user-id]
  (if (contains? players user-id)
    session ;; adding of existing player changes nothing
    (let [[starter-pack rest-modules] (split-at 5 modules)]
      (-> session
          (update :players assoc user-id (->player starter-pack))
          (assoc :modules rest-modules)))))
