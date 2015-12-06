(ns cyrats.domain.game-test
  (:require [cyrats.domain.defaults :refer :all]
            [cyrats.domain.game :as game]
            [clojure.test :refer :all]))

(def process-resources #'game/process-resources)
(def collect-deads #'game/collect-deads)

(deftest test-session
  (testing "resources-processing"
    (with-redefs [*DEFAULTS* (assoc *DEFAULTS*
                                    :initial-food 5
                                    :initial-energy 5
                                    :starving-rate 1
                                    :energize-rate 2)]
      (let [p (-> (game/->session (fn [_]))
                  (game/add-player :some-id)
                  process-resources
                  :players
                  :some-id)]
        (is (= 4 (p :food)))
        (is (= 7 (p :energy))))))

  (testing "collecting of deads"
    (let [s (-> (game/->session (fn [_]))
                (game/add-player :lucky)
                (game/add-player :deadman)
                (update-in [:players :deadman] assoc :food 0)
                collect-deads)]
      (is (contains? (s :players) :lucky))
      (is (not (contains? (s :players) :deadman)))
      (is (= 1 (count (:deads s)))))))
