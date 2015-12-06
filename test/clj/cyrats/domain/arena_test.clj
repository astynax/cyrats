(ns cyrats.domain.arena-test
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.arena :as arena]
            [cyrats.domain.rat :as rat]
            [cyrats.domain.rat-test :refer [mk-rat drop-1st-trophy]]
            [clojure.test :refer :all]))

(def R1 (mk-rat [[2 3 0] [2 3 0] [0 0 0]])) ;; 4/6/0
(def R2 (mk-rat [[3 1 1] [3 1 1] [0 0 0]])) ;; 6/2/2

(defn same?
  [{a1 :attack d1 :defence u1 :user-id}
   {a2 :attack d2 :defence u2 :user-id}]
  (and (= a1 a2)
       (= d1 d2)
       (= u1 u2)))

(defn dead [r] (rat/damage r (:health r)))
(defn almost-dead [r] (rat/damage r (- (:health r) 0.1)))

;; private functions reexport
(def bite #'arena/bite)
(def confront #'arena/confront)
(def exchange-trophies #'arena/exchange-trophies)
(def interact #'arena/interact)
(def dispence-food #'arena/dispence-food)

(deftest test-arena
  (testing "adding"
    (is (= {0 :r1 1 :r2}
           (-> (arena/->arena (fn [_]))
               (arena/add-rat :r1)
               (arena/add-rat :r2)
               :rats))))

  (testing "confrontation"
    (with-redefs [arena/*randomize-damage* (fn [x] x)]
      (is (and (= 3.0 (bite R1 R2))
               (= 2.0 (bite R2 R1)))
          "danage calculating correctly")

      (let [[rr1 rr2] (confront R1 R2)]
        (is (and
             (= (:user-id R1) (:user-id R2))
             (= rr1 R1)
             (= rr2 R2))
            "Both rats from one user - both have no damage"))

      (let [[rr1 rr2] (confront R1 (assoc R2 :user-id :other))]
        (is (= 2.0 (:health rr1)))
        (is (= 3.0 (:health rr2))))))

  (with-redefs [rat/*drop-some* drop-1st-trophy]
    (testing "exchange-trophies"
      (let [r1 (rat/take-loot R1 :loot1)
            r2 (rat/take-loot R2 :loot2)]

        (is (= [r1 r2] (exchange-trophies r1 r2))
            "Both alive - each one stay with its own stuff")

        (is (= [(dead r1) (dead r2)]
               (exchange-trophies (dead r1) (dead r2)))
            "Both dead - each one stay with its own stuff")

        (let [[rr1 rr2] (exchange-trophies r1
                                           (dead r2))]
          (is (= [:loot1 :loot2] (:backpack rr1)))
          (is (= [] (:backpack rr2))))

        (let [[rr1 rr2] (exchange-trophies (dead r1)
                                           r2)]
          (is (= [] (:backpack rr1)))
          (is (= [:loot2 :loot1] (:backpack rr2))))

        (let [rr1 (-> r1
                      (rat/take-loot :foo)
                      (rat/take-loot :bar))
              rr2 (dead r2)]
          (is (= [rr1 rr2]
                 (exchange-trophies rr1 rr2))
              "Life one have no more place for trophy - takes nothing")))))

  (testing "interact"
    (let [r1 (assoc R1 :user-id :other)
          a (-> (arena/->arena (fn [_]))
                (arena/add-rat r1)
                (arena/add-rat (almost-dead R2)))
          [aa deads] (interact a 0 1)]
      (is (= 1 (count deads)))
      (is (same? R2 (first deads)))
      (is (= 1 (count (:rats aa))))
      (is (same? r1 (first (vals (:rats aa)))))))

  (testing "dispence-food"
    (with-redefs [*DEFAULTS* (assoc *DEFAULTS*
                                    :food-taking-possibility 1.0)]
      (let [a (-> (arena/->arena (fn [_]))
                  (arena/add-rat (rat/take-loot R1 :loot))
                  (arena/add-rat (-> R2
                                     (rat/take-loot :a)
                                     (rat/take-loot :b)
                                     (rat/take-loot :c))))]
        (is (= [[:loot :food] [:a :b :c]]
               (->> (dispence-food a)
                    :rats
                    vals
                    (map :backpack))))))))
