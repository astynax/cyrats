(ns cyrats.server
  (:gen-class)
  (:require [org.httpkit.server :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [carica.core :refer [config]]))

(defroutes all-routes
  (GET "/" req
    (all-routes (assoc req :uri "/index.html")))

  (route/resources "")

  (route/not-found "Page not found :("))

(defn -main []
  (let [server-config (or (config :server)
                          {:port 8000})]
    (println (str "Server started at "
                  (server-config :port)
                  "..."))
    (run-server all-routes server-config)))
