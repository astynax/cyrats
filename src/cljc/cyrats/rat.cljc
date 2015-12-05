(ns cyrats.rat
  (:require
   [cyrats.utils :as utils]))

(defn ->rat
  "Rat constructor"
  [owner modules]
  {:modules (set modules)
   :owner owner
   :backpack []})

(defn stats
  [{modules :modules}]
  (utils/merge-module-stats modules))

(defn energy-required
  [rat]
  (apply + (vals (stats rat))))

;; arena rat
(defn is-alive?
  [{arena-hp :arena-hp}]
  (pos? arena-hp))

(defn can-loot?
  [{backpack :backpack}]
  (> 3 (count backpack)))

(defn is-complete?
  [{modules :modules}]
  (= 3 (count modules)))

(defn can-fight?
  [rat]
  (and (is-alive? rat) (can-loot? rat) (is-complete? rat)))

(defn add-item
  [rat item]
  (if (is-complete? rat)
    rat
    (update rat :backpack conj item)))

;;; arena
(defn prepare-for-arena
  [rat]
  (assoc rat :arena-hp (:hp (utils/merge-module-stats (:modules rat)))))
