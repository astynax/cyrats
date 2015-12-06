(ns cyrats.domain.defaults)

(def ^:dynamic *DEFAULTS*
  {:starving-rate 1
   :energize-rate 2
   :initial-food 5
   :initial-energy 5})

(def ^:dynamic *MODULES*
  (for [hp (range 5)
        ap (range 5)
        dp (range 5)]
    [hp ap dp]))
