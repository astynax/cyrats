(ns cyrats.model-test
  (:require [cyrats.model :refer :all]
            [clojure.test :refer :all]))

(deftest test-f
  (testing "f works"
    (is (= (f 42) 42))))

