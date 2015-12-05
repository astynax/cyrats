(ns cyrats.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [cyrats.model :as m]))

(defonce STATE (atom nil))

(def NAVIGATION
  [["index" ["[ About ]" "/"]]
   ["profile" ["[ Profile ]" "/profile"]]])

(q/defcomponent NavItem
  [current? title uri]
  (d/a {:className (str "nav-item" (if current? " current" ""))
        :href uri}
       title))

(q/defcomponent NavigationBar
  [current-page]
  (d/div {:id "nav-bar"}
         (d/div {:className "rat-img"} (d/div {:className "left rat2"}))
         (apply
          d/div {:id "nav-bar-items"}
          (d/a {:className "logo"
                :href "/"}
               ".:CyRats:.")
          (for [[page-name [title uri]] NAVIGATION]
            (NavItem (= page-name current-page) title uri)))
         (d/div {:className "rat-img"} (d/div {:className "left rat1"}))))

(q/defcomponent Footer
  []
  (d/div {:id "footer"}
         (d/div {:className "rat-img"} (d/div {:className "left rat1"}))
         (d/div {:id "footer-items"}
                (d/span {} "Copyright 2015, REPLicantus"))
         (d/div {:className "rat-img"} (d/div {:className "right rat3"}))))

(q/defcomponent IndexFrame
  [state]
  (d/div {:className "frame index"}
         (d/pre {} "Rust... Rust never changes. It creeps into your home, your things, your thoughts.
Even food tastes a little rusty. But who I am to complain?
At least I _have_ food, what little amount my CyberRats(tm) can scrounge from the surface.

And so it goes, Great Cycle Of Life:

Build. Wait. Eat.
Rinse. Repeat.
Die.")
         (d/img {:src "static/battleground.png"})
         ))

(q/defcomponent SideBar
  [state]
  (d/div {:id "side-bar"}
         (d/ul {})))

(q/defcomponent Window
  [{:keys [page] :as state}]
  (d/div {:id "root"}
         (NavigationBar page)
         (d/div {:id "content"}
                (({:index IndexFrame} page) state)
                (SideBar state))
         (Footer)))

(let [root (.getElementById js/document "react")]
  (defn refresh
    [state]
    (q/render (Window state)
              root)))

(add-watch STATE nil (fn [_ _ _ state]
                       (refresh state)))

(reset! STATE {:page :index})
