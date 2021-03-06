(ns cyrats.sockets
  (:require [chord.client :refer [ws-ch]]
            [cyrats.local-storage :as storage]
            [cljs-uuid-utils.core :as uuid]
            [cyrats.messages :as messages]
            [taoensso.timbre :as log]
            [cyrats.state :refer [STATE]]
            [cljs.core.async :refer [<! >! put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def SOCKET (atom nil))

(def handlers-map) ;; use for message dispatching

(defn register-channel [ws-ch]
  (log/debug "Saving socket")
  (reset! SOCKET ws-ch))

(defn handle-server-message [message]
  (if-let [handler (messages/message->handler message handlers-map)]
    
    (do
      (log/debug "DISPATCHED MESSAGE " message)
      (handler message))
    (log/debug "No handler for " message)))

(defn handle-server-error [error]
  (log/debug "Have error " error))

(defn listen-channel [ws-ch]
  (log/debug "Waiting for incoming")
  (go
    (loop []
      (if-let [{:keys [message error] :as msg} (<! ws-ch)]
        (cond
          message (do (handle-server-message message)
                      (recur))
          error (handle-server-error error))))))

(defn send-message [message]
  (log/debug "Sending " message)
  (go
    (>! @SOCKET message)))

(defn generate-uuid []
  (uuid/uuid-string (uuid/make-random-uuid)))

(defn- get-session-id []
  (if-let [session-id (storage/get-item "cyrats-session-id")]
    session-id
    (do
      (let [new-session-id (generate-uuid)]
        (storage/set-item! "cyrats-session-id" new-session-id)
        new-session-id))))


(defn connect-ws []
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:8000/ws"))] ;; FUCK
      (if-not error
        (do
          (log/debug "Connected!")
          (register-channel ws-channel)
          (send-message (messages/build :auth (get-session-id)))
          (listen-channel ws-channel))        
        (log/debug "Error:" (pr-str error))))))


(defonce _ (do
             (connect-ws)))


(defonce handlers-map {
                       :debug (fn [message]
                                (log/debug "Debug answer " message))
                       :arena-event (fn [[_ message]]
                                      (log/debug "NEW MESsSAGE " message)
                                      (swap! STATE
                                             (fn [state] (let [old-messages (state :messages)
                                                               new-messages (conj old-messages message)]
                                                           (assoc state :messages new-messages)                                                           
                                                           )

                                               )))
                       :state (fn [[_ state]]
                                (log/debug "SET STATE " state)
                                (swap! STATE
                                       #(-> @STATE
                                            (assoc :arenas (state :arenas))
                                            (assoc :messages (state :messages))
                                            )
                                       )
                                (log/debug "NEW STATE " @STATE))})


