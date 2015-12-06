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
             [cyrats.sockets :refer [register-socket]]
             [cyrats.socket-handlers :refer [socket-loop]]
             ))

(defn ws-handler [req]
  (log/info "Upgrading WS handler")
  (chord/with-channel req ws-ch
    (go
      (let [ {message :message}  (<! ws-ch)
            [_ session-id] message]
        (log/info "SESSION ID " session-id)
        (register-socket session-id ws-ch)
        (socket-loop session-id)))))

(defroutes application
  (GET "/" req
       (let [response (application (assoc req :uri "/index.html"))
             session (req :session)]
         (log/info "REQUEST " req)
         (assoc response :session session)))

  (GET "/arena/:id" req
       (let [response (application (assoc req :uri "/index.html"))
             session (req :session)]
         (log/info "REQUEST " req)
         (assoc response :session session)))

  (route/resources "")
  (GET "/ws" [] ws-handler)
  (route/not-found "Page not found :("))

