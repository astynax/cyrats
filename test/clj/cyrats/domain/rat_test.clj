(ns cyrats.domain.rat-test
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.rat :as rat]
            [cyrats.domain.game :as game]
            [clojure.test :refer :all]))

(defn mk-rat
  [vecs]
  (rat/->rat
   :some-user-id
   (mapv (partial apply game/->module) vecs)))

(defn some-rat
  []
  (mk-rat (take 3 (shuffle *MODULES*))))

(defn drop-1st-module [[m & ms] ts] [m (vec ms) ts])
(defn drop-1st-trophy [ms [t & ts]] [t ms (vec ts)])

(deftest test-rat
  (testing "rat creation"
    (let [r (mk-rat [[1 0 1]
                     [2 3 0]
                     [2 2 4]])]
      (is (= 5.0 (r :health)))
      (is (= 5 (r :attack)))
      (is (= 5 (r :defence)))
      (is (= 15 (r :energy-usage)))))

  (testing "aliveness and damage"
    (is (= 0.0 (:health (rat/damage (some-rat) 1000))))
    (is (every? (comp not rat/alive?)
                (for [_ (range 10)]
                  (let [r (some-rat)]
                    (rat/damage r (:health r)))))))

  (testing "looting"
    (is (= [42] (:backpack (rat/take-loot (some-rat) 42))))
    (is (not (rat/can-loot?
              (-> (some-rat)
                  (rat/take-loot nil)
                  (rat/take-loot nil)
                  (rat/take-loot nil)))))
    (is (thrown? AssertionError
                 (-> (some-rat)
                     (rat/take-loot nil)
                     (rat/take-loot nil)
                     (rat/take-loot nil)
                     (rat/take-loot nil)))))

  (testing "trophies"
    (let [modules [[1 2 3]
                   [3 2 1]
                   [0 1 0]]
          loot1 [0 0 0]
          loot2 42
          r (-> (mk-rat modules)
                (rat/take-loot loot1)
                (rat/take-loot loot2))
          all-staff (set (concat (:backpack r)
                                 (:modules r)))]

      (let [[trophy _] (rat/drop-trophy r)]
        (is (contains? all-staff trophy)))

      (with-redefs [rat/*drop-some* drop-1st-trophy]
        (let [[trophy {:keys [modules backpack]}] (rat/drop-trophy r)]
          (is (= loot1 trophy))
          (is (= modules (:modules r)))
          (is (= [loot2] backpack))))

      (with-redefs [rat/*drop-some* drop-1st-module]
        (let [[trophy {:keys [modules backpack]}] (rat/drop-trophy r)]
          (is (= (game/->module 1 2 3) trophy))
          (is (= modules (rest (:modules r))))
          (is (= [loot1, loot2] backpack)))))))
