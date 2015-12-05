(ns cyrats.model-test
  (:require [cyrats.model :refer :all]
            [clojure.test :refer :all]))

(deftest test-module-create
  (testing "module creation"
    (is (= (->module 10 20 30) {:hp 10 :ap 20 :dp 30}))))

(def module1 (->module 1 2 3))
(def module2 (->module 10 20 30))
(def module3 (->module 100 200 300))



(deftest test-merge-module-stats
    (testing "stats merge"
        (is (= (merge-module-stats [module1 module2 module3]) (->module 111 222 333)))))

(deftest test-rat-create
  (testing "rat creation"
    (is (= (->rat :owner [module1 module2 module3])
           {:modules [module1 module2 module3]
            :backpack []
            :owner :owner}))))

(def default-rat (->rat :owner [module1 module2 module3]))

(deftest test-prepare-for-arena
  (testing "check :arena-hp"
    (is (= (:arena-hp (prepare-rat-for-arena default-rat)) 111))))


(deftest test-rat-stats
  (testing "rat stats"
      (is (= (rat-stats default-rat)
             {:hp 111
              :ap 222
              :dp 333}))))


(deftest test-rat-energy-required
  (testing "rat sum of all stats"
    (is (= (rat-energy-required default-rat) 666))))

(deftest test-rat-alive
  (testing "testing rat positive hp"
    (is (true? (rat-is-alive? (assoc default-rat :arena-hp 100)))))
  (testing "testing rat zero hp"
    (is (false? (rat-is-alive? (assoc default-rat :arena-hp 0)))))
  (testing "testing rat negative hp"
    (is (false? (rat-is-alive? (assoc default-rat :arena-hp -100))))))

(deftest test-rat-can-loot
  (testing "testing rat empty backpack"
    (is (true? (rat-can-loot? default-rat))))
  (testing "testing rat not full backpack"
    (is (true? (rat-can-loot? (update-in default-rat [:backpack] conj 1)))))
  (testing "testing rat full backpack"
    (is (false? (rat-can-loot? (update default-rat :backpack conj 1 2 3)))))
  (testing "testing rat overflow backpack"
    (is (false? (rat-can-loot? (update default-rat :backpack conj 1 2 3 4))))))

;; (deftest test-rat-can-fight
;;   (testing "rat full backpack"
;;     (let [rat (->rat (->module 10 20 30) (->module 1 2 3) (->module 11 12 13))]
;;       (is (= (rat-stats rat)
;;              {:hp 22
;;               :ap 34
;;               :dp 46}))))
;;   (testing "rat stats"
;;     (let [rat (->rat (->module 10 20 30) (->module 1 2 3) (->module 11 12 13))]
;;       (is (= (rat-stats rat)
;;              {:hp 22
;;               :ap 34
;;               :dp 46}))))
;;   )
