(ns cyrats.state
  (:require [clojure.core.async :refer [go close! <! timeout alts!!]]
            [taoensso.timbre :as log]
            [clojure.core.match :refer [match]])
  )


(def STATE (atom {:arenas [
                           [1 "Arena 1"]
                           [2 "Arena 2"]
                           [3 "Arena 3"]
                           [4 "Arena 4"]]}))


(def SUBSCRIPTIONS (atom {
                          1 #{}
                          2 #{}
                          3 #{}
                          4 #{}
                          }))


(defn get-state [] @STATE)

(defn subscribe-arena [session-id arena-id]
  (let [new-subscribers (conj (@SUBSCRIPTIONS arena-id) session-id)]
    (swap! SUBSCRIPTIONS assoc arena-id new-subscribers)))

(defn unsubscribe-arena [session-id arena-id]
  (let [new-subscribers (disj (@SUBSCRIPTIONS arena-id) session-id)]
    (swap! SUBSCRIPTIONS assoc arena-id new-subscribers)))


(def TICK 5000)

(defn send-subscriptions []
  (log/debug "SENDING SUBSCRIPTIONS"))

(defn handle-subscriptions [stop-channel]
  (go
    (loop []
      (let [[v ch] (alts!! [stop-channel (timeout TICK)])]
        (match ch
          stop-channel (log/debug "Stopping handle-subscriptions routine")
          :else (do
                  (send-subscriptions)
                  (recur)))))))
      
      
      
