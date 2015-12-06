(ns cyrats.state)


(def STATE (atom {:arenas [
                           [1 "Arena 1"]
                           [2 "Arena 2"]
                           [3 "Arena 3"]
                           [4 "Arena 4"]]
                  :messages [
                             {:arena-id 1 :payload "Message1"}
                             {:arena-id 1 :payload "Message2"}
                             {:arena-id 1 :payload "Message3"}
                             {:arena-id 1 :payload "Message4"}
                             {:arena-id 1 :payload "Message5"}
                             {:arena-id 1 :payload "Message6"}
                            ]}))




(defn get-state [] @STATE)


