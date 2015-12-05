(ns cyrats.sockets
  (:require [chord.client :refer [ws-ch]]
            [cyrats.local-storage :as storage]
            [cljs-uuid-utils.core :as uuid]
            [cljs.core.async :refer [<! >! put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(def SOCKET (atom nil))

(defn register-channel [ws-ch]
  (js/console.log "Saving socket")
  (reset! SOCKET ws-ch))

(defn handle-server-message [message error]
  (.log js/console "M " message " Er " error))

(defn listen-channel [ws-ch]
  (.log js/console "Waiting for incoming")
  (go
    (loop []
      (.log js/console "BEFORE")
      (let [{:keys [message error] :as msg} (<! ws-ch)]
        (when message
          (handle-server-message message error)
          (recur)
          )
        )
      )
    ))

(defn send-message [type payload]
  (.log js/console "Send " type " with " payload)
  (go
    (>! @SOCKET [type payload])))

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
    (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:8080/ws"))]
      (if-not error
        (do
          (js/console.log "Connected!")
          (register-channel ws-channel)
          (send-message :auth (get-session-id))
          (listen-channel ws-channel))        
        (js/console.log "Error:" (pr-str error))))))


(defonce _ (do
             (connect-ws)))



