(ns cyrats.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [cyrats.model :as m]))

(def NAVIGATION
  [["index" ["[ About ]" "/"]]
   ["rooms" ["[ Rooms ]" "/rooms"]]
   ["profile" ["[ Profile ]" "/profile"]]])

(q/defcomponent NavItem
  [current? title uri]
  (d/a {:className (str "nav-item" (if current? " current" ""))
        :href uri}
       title))

(q/defcomponent NavigationBar
  [current-page]
  (letfn [(rat-img [uri] (d/img {:className "rat-img"
                                 :src uri}))]
    (apply
     d/div {:id "nav-bar"}
     (rat-img "static/rat2.png")
     (d/a {:className "logo"
           :href "/"}
          "CyRats")
     (concat
      (for [[page-name [title uri]] NAVIGATION]
        (NavItem (= page-name current-page) title uri))
      [(rat-img "static/rat1.png")]))))


(defn ^:global do-navigation
  [page]
  (q/render (NavigationBar page)
            (.getElementById js/document "navigation")))

(defn ^:global do-content
  [page]
  )
