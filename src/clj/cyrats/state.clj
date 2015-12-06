(ns cyrats.state)


(def STATE (atom {:arenas [
                           [1 "Arena 1"]
                           [2 "Arena 2"]
                           [3 "Arena 3"]
                           [4 "Arena 4"]]}))




(defn get-state [] @STATE)


