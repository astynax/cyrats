(ns cyrats.sockets
  (:require
   [clojure.core.async :refer [>! <! go close!]]
   [taoensso.timbre :as log]
   [cyrats.messages :as messages]
   [cyrats.state :as state]
   ))

(def CLIENTS (atom {}))

(defn send->socket [message ws-ch]
  (log/info "Sending " message " to " ws-ch)
  (go
    (>! ws-ch message)))

(defn send->user [message user-id]
  (let [ws-ch (@CLIENTS user-id)]
    (send->socket message ws-ch)))

(defn init-user [session-id]
  (log/info "Will init session for " session-id)
  (if-let [ws-ch (@CLIENTS session-id)]
    (do
      (log/info "Sending world state to " session-id)
      (let [state-message (messages/build :state (state/get-state))
            page-message (messages/build :page (@state/PAGES session-id :index))]
        (send->socket state-message ws-ch)
        (send->socket page-message ws-ch)))
    (log/info "Have no session for " session-id)))

(defn register-socket [session-id ws-ch]
  (log/info "Registring socket for " session-id)
  (if-let [old-socket (@CLIENTS session-id)]
    (do
      (log/info "Closing previous socket for " session-id)
      (close! old-socket)))
  (swap! CLIENTS assoc session-id ws-ch)
  (init-user session-id))

(defn unregister-socket [session-id]
  (log/debug "Closing socket for " session-id)
  (if-let [ws-ch (@CLIENTS session-id)]
    (close! ws-ch)
    (swap! CLIENTS dissoc session-id)))

(defn reinit-all-users []
  (doseq [user-id (keys @CLIENTS)]
    (init-user user-id)))

(add-watch state/STATE :reinit-users (fn [_ _ _ _]
                                       (reinit-all-users)))
