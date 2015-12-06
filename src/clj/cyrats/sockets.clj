(ns cyrats.sockets
  (:require
   [clojure.core.async :refer [>! <! go close!]]
   [taoensso.timbre :as log]
   [cyrats.messages :as messages]
   [cyrats.state :as state]
   [cyrats.arenas :as arenas]

   ))

(def handlers-map)
(def CLIENTS (atom {}))

(defn send->socket [answer ws-ch]
  (log/info "Sending " answer " to " ws-ch)
  (go
    (>! ws-ch answer)))

(defn socket-loop [session-id]
  (let [ws-ch (@CLIENTS session-id)]
    (go
      (loop []
        (if-let [raw-message (<! ws-ch)] ;; split me into separate functions
          (do  ;; this to handle-message
            (let [
                  message (messages/socket-data->message raw-message)
                  handler (messages/message->handler message handlers-map)
                  
                  ]
              (if handler
                (do
                  (log/info "Will handle with " handler)
                  (if-let [answer (handler session-id message)]
                    (send->socket answer ws-ch)))
                (log/debug "No handler for " message)))
            (recur))
          (do ;; this to close-socket
            (log/debug "Closing socket for " session-id)
            (close! ws-ch)
            (swap! CLIENTS dissoc session-id)))))))


(defn init-user [session-id]
  (log/info "Will init session for " session-id)
  (if-let [ws-ch (@CLIENTS session-id)]
    (do
      (log/info "Sending world state to " session-id)
      (let [message (messages/build :state
                                    (state/get-state))]
        (send->socket message ws-ch)))
    (log/info "Have no session for " session-id)))

(defn register-socket [session-id ws-ch]
  (log/info "Registring socket for " session-id)
  (if-let [old-socket (@CLIENTS session-id)]
    (do
      (log/info "Closing previous socket for " session-id)
      (close! old-socket)))
  (swap! CLIENTS assoc session-id ws-ch)
  (init-user session-id))





;; Debug
(def COUNTER (atom 0))

(defonce handlers-map {
                       :debug (fn [session-id message]
                                (swap! COUNTER inc)
                                (messages/build :debug {:answer @COUNTER})
                                )
                       :arena-subscribe (fn [session-id [_ arena-id]]
                                          (arenas/subscribe-arena session-id arena-id)
                                          (messages/build :subscribed :ok))
                       :arena-unsubscribe (fn [session-id [_ arena-id]]
                                            (arenas/unsubscribe-arena session-id arena-id)
                                            (messages/build :unsubscribed :ok)
                                            )
                       })
