(ns cyrats.model-test
  (:require [cyrats.model :refer :all]
            [clojure.test :refer :all]))

(deftest test-module-create
  (testing "module creation"
    (is (= (->module 10 20 30) {:hp 10 :ap 20 :dp 30}))))

(def module1 (->module 1 2 3))
(def module2 (->module 10 20 30))
(def module3 (->module 100 200 300))
(def default-modules [module1 module2 module3])

(deftest test-merge-module-stats
    (testing "stats merge"
        (is (= (merge-module-stats default-modules) (->module 111 222 333)))))

(deftest test-rat-create
  (testing "rat creation"
    (is (= (->rat :owner default-modules)
           {:modules default-modules
            :backpack []
            :owner :owner}))))

(def default-rat (->rat :owner default-modules))
(def default-arena-rat (assoc default-rat :arena-hp 100))

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

(deftest test-rat-can-fight
  (testing "testing rat (hp>0 modules=3 backpack<3)"
    (is (true? (rat-can-fight? default-arena-rat))))
  (testing "testing rat (hp=0 modules=3 backpack<3)"
    (is (false? (rat-can-fight? (assoc default-arena-rat :arena-hp 0))))))
  ;; TODO more can-fight tests


(deftest test-player-create
  (testing "player creation"
    (is (= (->player default-modules 10 20 false)
           {:modules default-modules
            :food 10
            :energy 20
            :rats []
            :bot? false}))))

(def default-player (->player (conj default-modules (->module 0 0 0)) 10 20 false))

(deftest test-player-is-alive
  (testing "testing player positive food"
    (is (true? (player-is-alive? (assoc default-player :food 100)))))
  (testing "testing player zero food"
    (is (false? (player-is-alive? (assoc default-player :food 0)))))
  (testing "testing player negative food"
    (is (false? (player-is-alive? (assoc default-player :food -100))))))

(deftest test-player-can-assemble
  (testing "testing player can assemble normal rat "
    (is (true? (player-can-assemble-rat? default-player default-modules))))
  (testing "testing player can't assemble small rat "
    (is (false? (player-can-assemble-rat? default-player [(->module 1 1 1)]))))
  (testing "testing player can't assemble big rat "
    (is (false? (player-can-assemble-rat? default-player (conj default-modules (->module 1 1 1) (->module 2 2 2))))))
  (testing "testing player can't assemble rat with unknown modules"
    (is (false? (player-can-assemble-rat? default-player (vec (repeat 3 (->module 1 1 1))))))))
