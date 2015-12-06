(ns cyrats.domain.defaults)

(def ^:dynamic *DEFAULTS*
  {:starving-rate 1
   :energize-rate 2
   :initial-food 5
   :initial-energy 5
   :confront-possibility 0.3
   :food-taking-possibility 0.2
   })

(def ^:dynamic *MODULES*
  (for [hp (range 5)
        ap (range 5)
        dp (range 5)]
    [hp ap dp]))
