(ns cyrats.web
  (:require  [org.httpkit.server :refer :all]
             [compojure.core :refer :all]
             [compojure.route :as route]
             [chord.http-kit :as chord]
             [ring.util.response :as response]
             [taoensso.timbre :as log]
             [clojure.core.async :refer [>! <! go close!]]
             [cyrats.state :as state]
             [cyrats.messages :as messages]
             ))

(def handlers-map)

(def CLIENTS (atom {}))

(defn send->socket [answer ws-ch]
  (log/info "Sending " answer " to " ws-ch)
  (go
    (>! ws-ch answer)))

(defn socket-loop [ws-ch]
  (go
    (loop []
      (if-let [raw-message (<! ws-ch)]
        (do
          (let [
                message (messages/socket-data->message raw-message)
                handler (messages/message->handler message handlers-map)
                
                ]
            (if handler
              (do
                (log/info "Will handle with " handler)
                (if-let [answer (handler message)]
                  (send->socket answer ws-ch)))
              (log/debug "No handler for " message)))))
      (recur))))

(defn init-user [session-id]
  (log/info "Will init session for " session-id)
  (if-let [ws-ch (@CLIENTS session-id)]
    (do
      (log/info "Sending world state to " session-id)
      (let [message (messages/build :state
                                    (state/get-state))]
        (send->socket message ws-ch)
        ))
    (log/info "Have no session for " session-id)))


(defn register-socket [session-id ws-ch]
  (log/info "Registring socket for " session-id)
  (if-let [old-socket (@CLIENTS session-id)]
    (do
      (log/info "Closing previous socket for " session-id)
      (close! old-socket)))
  (swap! CLIENTS assoc session-id ws-ch)
  (init-user session-id)
  )

(defn ws-handler [req]
  (log/info "Upgrading WS handler")
  (chord/with-channel req ws-ch
    (go
      (let [ {message :message}  (<! ws-ch)
            [_ session-id] message]
        (log/info "SESSION ID " session-id)
        (register-socket session-id ws-ch)
        ))
    (socket-loop ws-ch)
    ))

(defroutes application
  (GET "/" req
       (let [response (application (assoc req :uri "/index.html"))
             session (req :session)]
         (log/info "REQUEST " req)
         (assoc response :session session)))
  (route/resources "")
  (GET "/ws" [] ws-handler)
  (route/not-found "Page not found :("))

(defn event-handler [message]
  (log/info "Do nothing with " message)
  "Do nothing")


(def COUNTER (atom 0))
(defonce handlers-map {:event event-handler
                       :debug (fn [message]
                                (swap! COUNTER inc)
                                (messages/build :debug {:answer @COUNTER})
                                )
                       })

