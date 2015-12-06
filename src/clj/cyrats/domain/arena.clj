(ns cyrats.domain.arena
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.rat :as rat]))

(defn ->arena
  [log-fn]
  {:id-cnt 0
   :rats {}
   :log-fn log-fn})

(defn add-rat
  [{:keys [id-cnt log-fn] :as arena} rat]
  (log-fn [:rat-added-to-arena rat])
  (-> arena
      (update :rats assoc id-cnt rat)
      (assoc :id-cnt (inc id-cnt))))

;; rats confrontation

(defn- ^:dynamic *randomize-damage*
  [dmg]
  (* dmg (+ 0.75 (rand 0.75))))

(defn- bite
  "Returns amount of damage doing by attacker to target"
  [attacker target]
  (let [ap (:attack attacker)
        dp (:defence target)]
    (if (zero? ap)
      0
      (let [ap' (float ap)]
        (*randomize-damage*
         (if (zero? dp)
           ap'
           (/ ap' dp)))))))

(defn- confront
  "Two rats bite each other"
  [rat1 rat2]
  (if (= (:user-id rat1)
         (:user-id rat2))
    [rat1 rat2]
    (let [dmg2 (bite rat1 rat2)
          dmg1 (bite rat2 rat1)]
      [(rat/damage rat1 dmg1)
       (rat/damage rat2 dmg2)])))

(defn- move-trophy
  "First rat takes the trophy from second"
  [r1 r2]
  (let [[trophy new-r2] (rat/drop-trophy r2)]
    [(rat/take-loot r1 trophy) new-r2]))

(defn- exchange-trophies
  "Two rats take the trophies (if can and should)"
  [rat1 rat2]
  (let [alive1? (rat/alive? rat1)
        alive2? (rat/alive? rat2)]
    (if (= alive1? alive2?) ;; both true/false
      [rat1 rat2]
      (if alive1?
        (if (rat/can-loot? rat1)
          (move-trophy rat1 rat2)
          [rat1 rat2])
        (if (rat/can-loot? rat2)
          (let [[r2 r1] (move-trophy rat2 rat1)]
            [r1 r2])
          [rat1 rat2])))))

(defn- interact
  [{:keys [log-fn] :as arena} rat1-id rat2-id]
  (let [rat1 (get-in arena [:rats rat1-id])
        rat2 (get-in arena [:rats rat2-id])

        [new-rat1 new-rat2]
        (->> [rat1 rat2]
             (apply confront)
             (apply exchange-trophies)
             ;; TODO: add module damaging and destruction
             )]
    (log-fn [:rats-interact rat1 rat2])
    ;; returns
    (let [[new-arena dead]
          (reduce (fn [[a d] [id r]]
                    (if (rat/alive? r)
                      [(assoc-in a [:rats id] r) d]
                      [(update a :rats dissoc id) (cons r d)]))
                  [arena nil]
                  [[rat1-id new-rat1]
                   [rat2-id new-rat2]])]

      (doseq [r dead]
        (log-fn [:rat-dead r]))

      [new-arena dead])))

(defn- select-interacting-pairs
  "Selects some pairs of rats to confront"
  [{:keys [rats]}]
  (let [rat-count (count rats)
        gain (*DEFAULTS* :confront-possibility)]
    (when (> rat-count 1)
      (->> rats
           keys
           shuffle
           (partition 2)
           (filter (fn [_] (< (rand 1) gain)))))))

(defn- interact-selected
  "Do interaction between some pairs of rats"
  [arena]
  (let [pairs (select-interacting-pairs arena)]
    (let [[new-arena lists-of-deads]
          (reduce (fn [[a ds] [r1 r2]]
                    (let [[aa deads] (interact a r1 r2)]
                      [aa (cons deads ds)]))
                  [arena nil]
                  pairs)]
      [new-arena (concat lists-of-deads)])))

(defn- dispence-food
  "Places some food to arena (places it into rat backpacks)"
  [{:keys [rats] :as arena}]
  (let [gain (*DEFAULTS* :food-taking-possibility)
        luckies
        (->> rats
             (filter (fn [[_ r]] (and (rat/can-loot? r)
                                      (< (rand 1) gain))))
             (map first))]
    (reduce (fn [a id]
              (update-in a [:rats id] rat/take-loot :food))
            arena
            luckies)))

(defn populate
  "Does one phase of arena's live"
  [arena]
  (-> arena
      dispence-food
      interact-selected))
