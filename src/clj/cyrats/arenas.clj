(ns cyrats.arenas
  (:require [cyrats.messages :as messages]
            [clojure.core.match :refer [match]]
            [taoensso.timbre :as log]
            [cyrats.sockets :as sockets]
            [cyrats.state :refer [STATE]]
            [clojure.core.async :refer [go close! <! >! timeout alts!! chan]]
            ))

(def SUBSCRIPTIONS (atom {
                          1 #{}
                          2 #{}
                          3 #{}
                          4 #{}
                          }))

(def EVENTS-CHANNEL (chan))

(defn new-arena [name]
  (swap! STATE (fn [state]
                 (let [old-arenas (state :arenas)
                       new-id (+ 1 (apply max (for [[arena-id _] old-arenas] arena-id)))
                       new-arena [new-id name]
                       new-arenas (conj old-arenas new-arena)
                       old-messages (state :messages)
                       created-message {:arena-id new-id :payload "Created new area"}
                       new-messages (conj old-messages created-message)
                       ]
                   (-> state
                       (assoc :arenas new-arenas)
                       (assoc :messages new-messages)
                       )))))

(defn new-event [arena-id payload]
  (let [message {:arena-id arena-id
                 :payload payload}]
    (swap! STATE (fn [state]
                   (let [old-messages (state :messages) ;; TODO: сделать скольжащее очищение 
                         new-messages (conj old-messages message)]
                     (assoc state :messages new-messages)
                     )))
    ))


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
        outgoing-message (messages/build :arena-event event)
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
