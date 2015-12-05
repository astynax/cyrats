(ns cyrats.game
  (:require
   [clojure.math.combinatorics :refer [selections]]
   [clojure.set :as set]))


;; utils
(defn log-message [game message]
  (update-in game [:messages] (fn [v] (conj v message))))

(defn clear-messages
  [game]
  (update-in game [:messages] (fn [v] (vec []))))

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



(defn make-modules-for-game []
  (map #(apply ->module %) (shuffle (selections [0 1 2 3 4] 3))))

(defn stats
  [{modules :modules}]
  (merge-module-stats modules))

;;; rat

(defn ->rat
  "Rat constructor"
  [owner modules]
  {:modules (set modules)
   :owner owner
   :backpack []})

(defn rat-energy-required
  [rat]
  (apply + (vals (stats rat))))

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

(defn rat-add-item
  [rat item]
  (if (rat-is-complete? rat)
    rat
    (update rat :backpack conj item)))

(defn rat-prepare-for-arena
  [rat]
  (assoc rat :arena-hp (:hp (merge-module-stats (:modules rat)))))

;;; player

(defn ->player [modules name food energy]
  {:modules (set modules)
   :food food
   :name name
   :energy energy})

(defn player-tick [player]
  (-> player
      (update :food - 10)
      (update :energy + 10)))

(defn player-is-alive? [{food :food}]
  (pos? food))

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


;;; game
(defn ->game [players]
  (let [modules (partition 5 (make-modules-for-game))]
    {:player (map #(apply ->player % %2 10 20) (map vector players modules))
     :messages []}))


(->game [1 2 3 4])


;; (defn give-init-modules [players modules cnt]
;;   (map #(update % :modules (take cnt 
