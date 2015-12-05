(ns cyrats.model-test
  (:require [cyrats.model :refer :all]
            [clojure.test :refer :all]))

(deftest test-module-create
  (testing "module creation"
    (is (= (->module 10 20 30) {:hp 10 :ap 20 :dp 30}))))

(deftest test-merge-module-stats
    (testing "stats merge"
      (let [mod1 (->module 10 20 30)
            mod2 (->module 33 44 55)]
        (is (= (merge-module-stats [mod1 mod2]) (->module 43 64 85))))))

(deftest test-rat-create
  (testing "rat creation"
    (is (= (->rat (->module 10 20 30) (->module 1 2 3) (->module 11 12 13))
           {:modules [(->module 10 20 30) (->module 1 2 3) (->module 11 12 13)]
            :backpack []
            }))))

(deftest test-rat-stats
  (testing "rat stats"
    (let [rat (->rat (->module 10 20 30) (->module 1 2 3) (->module 11 12 13))]
      (is (= (rat-stats rat)
             {:hp 22
               :ap 34
               :dp 46})))))
