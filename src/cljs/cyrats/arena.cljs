(ns cyrats.arena
  (:require [cyrats.state :refer [STATE]]
            [taoensso.timbre :as log]
            [cyrats.messages :as messages]
            [cyrats.sockets :as sockets]))

(defn unsubscribe-arena [arena-id]
  (let [message (messages/build :arena-unsubscribe arena-id)]
    (sockets/send-message message))
  (log/debug "Ubsubscribed from arena " arena-id))

(defn subscribe-arena [arena-id]
  (let [message (messages/build :arena-subscribe arena-id)]
    (sockets/send-message message))
  (log/debug "Subscribed to arena " arena-id))
