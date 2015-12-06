(ns cyrats.system
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]
            [compojure.handler :refer [site]]
            [carica.core :refer [config]]
            [cyrats.web :refer [application]]
            [cyrats.arenas :as arenas]
            [clojure.core.async :refer [go close! chan]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [taoensso.timbre :as log]))

(defonce SESSION-STORE (cookie-store))

(defn- start-server [handler port]
  (let [server (run-server (site handler
                                 ) {:port port})]
    (println (str "Started server on localhost:" port))
    server))

(defn- stop-server [server]
  (when server
    (server))) ;; run-server returns a fn that stops itself

(defrecord CyratServer [port]
  component/Lifecycle
  (start [this]
    (let [stop-channel (chan)
          subscription-handler (arenas/handle-subscriptions stop-channel)]
      (-> this
          ( assoc :server (start-server #'application port))
          ( assoc :stop-channel stop-channel)
          ( assoc :test "TEST"))))
  
  (stop [this]
    (println this)
    (stop-server (:server this))
    (close! (:stop-channel this))
    

    (dissoc this :stop-channel)))

(defn create-system [port]
  (CyratServer. port))

(defn -main [& args]
  (let [server-config (or (config :server)
                          {:port 8080})]
    
    (.start (create-system (:port server-config)))
    ))
