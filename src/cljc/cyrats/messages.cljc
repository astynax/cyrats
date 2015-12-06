(ns cyrats.messages
  (:require  [taoensso.timbre :as log]))


(defn message->handler [[message-type payload] handlers-map]
  (log/debug "Dispatching message " message-type " with " payload)
  (if-let [handler (message-type handlers-map)]
    handler))

(defn build [message-type payload]
  (log/debug "Building message " message-type " payload " payload)
  [message-type payload])

(defn socket-data->message [raw-message]
  (log/debug "Parsing raw-message " raw-message)
  (raw-message :message))
