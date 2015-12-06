(ns cyrats.socket-handlers
  (:require [cyrats.messages :as messages]
            [cyrats.arenas :as arenas]
            [taoensso.timbre :as log]
            [clojure.core.async :refer [>! <! go close!]]
            [cyrats.sockets :as sockets]))

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
                                            (messages/build :unsubqscribed :ok)
                                            )
                       })


(defn socket-loop [session-id]
  (let [ws-ch (@sockets/CLIENTS session-id)]
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
                    (sockets/send->socket answer ws-ch)))
                (log/debug "No handler for " message)))
            (recur))
          (do ;; this to close-socket
            (log/debug "Closing socket for " session-id)
            (close! ws-ch)
            (swap! @sockets/CLIENTS dissoc session-id)))))))
