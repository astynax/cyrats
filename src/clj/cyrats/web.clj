(ns cyrats.web
  (:require  [org.httpkit.server :refer :all]
             [compojure.core :refer :all]
             [compojure.route :as route]
             ))


(defroutes application
  (GET "/" req
       (application (assoc req :uri "/index.html")))

  (route/resources "")

  (route/not-found "Page not found :("))

