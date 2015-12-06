(ns cyrats.domain.arena-test
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.arena :as arena]
            [cyrats.domain.rat :as rat]
            [cyrats.domain.rat-test :refer [mk-rat]]
            [clojure.test :refer :all]))

(deftest test-arena
  (testing "adding"
    (is (= {0 :r1 1 :r2}
           (-> (arena/->arena)
               (arena/add-rat :r1)
               (arena/add-rat :r2)
               :rats))))

  (testing "confrontation"
    (with-redefs [arena/*randomize-damage* (fn [x] x)]
      (let [r1 (mk-rat [[2 3 0] [2 3 0] [0 0 0]])  ;; 4/6/0
            r2 (mk-rat [[3 1 1] [3 1 1] [0 0 0]])] ;; 6/2/2

        (is (and (= 3.0 (arena/bite r1 r2))
                 (= 2.0 (arena/bite r2 r1)))
            "danage calculating correctly")

        (let [[rr1 rr2] (arena/confront r1 r2)]
          (is (and
               (= (:user-id r1) (:user-id r2))
               (= rr1 r1)
               (= rr2 r2))
              "Both rats from one user - both have no damage"))

        (let [[rr1 rr2] (arena/confront r1 (assoc r2 :user-id :other))]
          (is (= 2.0 (:health rr1)))
          (is (= 3.0 (:health rr2))))))))
