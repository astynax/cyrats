(ns cyrats.arenas
  (:require [cyrats.messages :as messages]
            [clojure.core.match :refer [match]]
            [taoensso.timbre :as log]
            [cyrats.sockets :as sockets]
            [clojure.core.async :refer [go close! <! >! timeout alts!! chan]]
            ))

(def SUBSCRIPTIONS (atom {
                          1 #{}
                          2 #{}
                          3 #{}
                          4 #{}
                          }))


(def ARENAS-EVENTS (atom {
                          1 []
                          2 []
                          3 []
                          4 []
                          }))

(def EVENTS-CHANNEL (chan))

(add-watch ARENAS-EVENTS :arena-subscriptions (fn [_ _ old-state state]))

(defn new-arena []
  "Do nothing, should put in state")

(defn new-event [arena-id payload]
  (go
    (>! EVENTS-CHANNEL {:arena-id arena-id
                        :payload payload})))


(defn subscribe-arena [session-id arena-id]
  (let [new-subscribers (conj (@SUBSCRIPTIONS arena-id) session-id)]
    (swap! SUBSCRIPTIONS assoc arena-id new-subscribers)))

(defn unsubscribe-arena [session-id arena-id]
  (let [new-subscribers (disj (@SUBSCRIPTIONS arena-id) session-id)]
    (swap! SUBSCRIPTIONS assoc arena-id new-subscribers)))

(defn send-subscriptions [event]
  (log/debug "HAVE MESSAGE " event)
  (let [
        {:keys [arena-id payload]} event
        outgoing-message (messages/build :arena-event [arena-id payload])
        subscribers (@SUBSCRIPTIONS arena-id)
        ]
    (log/debug "Subscribers " subscribers)
    (doseq [user-id subscribers]
      (sockets/send->user outgoing-message user-id)
      (log/debug "Answering to user " user-id " with " outgoing-message))))


(defn handle-subscriptions [stop-channel]
  (go
    (loop []
      (log/debug "Wairing for message")
      (let [[message ch] (alts!! [stop-channel EVENTS-CHANNEL])]
        (log/debug "Message  " message " from " ch)
        (match ch
          stop-channel (log/debug "Stopping handle-subscriptions routine")
          :else (do
                  (send-subscriptions message)
                  (recur)))))))
