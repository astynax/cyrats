(ns cyrats.domain.rat
  (:require [cyrats.domain.defaults :refer :all]))

(defn- total-stats
  [modules]
  (apply merge-with + modules))

(defn ->rat
  [user-id modules]
  {:pre [(= 3 (count modules))]}
  (let [{:keys [hp ap dp]} (total-stats modules)]
    {:player user-id
     :modules modules
     :backpack []
     :health (max (float hp)
                  0.01) ;; rats with HP=0 must be alive until 1st confrontation
     :attack ap
     :defence dp
     :energy-usage (+ hp ap dp)}))

(def alive? (comp pos? :health))

(def can-loot? (comp (partial > 3) count :backpack))

(defn take-loot
  [rat item]
  {:pre [(can-loot? rat)]}
  (update rat :backpack conj item))

;; damage

(defn damage
  [rat amount]
  (update rat
          :health
          (fn [h]
            (float
             (max 0 (- h amount))))))

;; trophy selection

(defn- reject-rand-nth
  [coll]
  (let [x (rand-nth coll)
        [heads [_ & tails]] (split-with #(not= % x) coll)]
    [x (vec (concat heads tails))]))

(defn ^:private ^:dynamic *drop-some*
  [modules backpack]
  (let [drop-from (if (seq backpack) (rand-nth [:m :b]) :m)]
    (case drop-from
      :m (let [[x xs] (reject-rand-nth modules)]
           [x xs backpack])
      :b (let [[x xs] (reject-rand-nth backpack)]
           [x modules xs]))))

(defn drop-trophy
  [rat]
  (let [{:keys [modules backpack]} rat
        [trophy new-modules new-backpack] (*drop-some* modules backpack)]
    [trophy (assoc rat
                   :modules new-modules
                   :backpack new-backpack)]))
