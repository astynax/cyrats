(ns cyrats.core
  (:require
   ;;[quiescent.core :as q]
   ;;[quiescent.dom :as d]
   [rum.core :as rum]
   [cyrats.model :as m]
   [cyrats.sockets :as sockets]
   [goog.events])
  (:import [goog.history Html5History EventType]))

(defonce STATE (atom {:page :index
                      :arenas [[1 "Arena 1"]
                               [2 "Arena 2"]
                               [3 "Arena 3"]]}))

;; browser history manipulation
;; source:
;; http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history

(defn- get-token []
  (str js/window.location.pathname
       js/window.location.search))

(defn- make-history []
  (doto (Html5History.)
    (.setPathPrefix (str js/window.location.protocol
                         "//"
                         js/window.location.host))
    (.setUseFragment false)))

(defn- handle-url-change [e]
  (let [token (get-token)]
    (js/console.log (str "Navigating: " token))
    (when-not (.-isNavigation e)
      (js/window.scrollTo 0 0))))

(defonce ^:private history
  (doto (make-history)
    (goog.events/listen EventType.NAVIGATE
                        #(handle-url-change %))
    (.setEnabled true)))

(defn- nav! [token page]
  (.setToken history token)
  (swap! STATE assoc :page page))

;; UI

(def NAVIGATION
  [[:about ["[ About ]" "/about"]]
   [:profile ["[ Profile ]" "/profile"]]])

(defn link
  [cls current-page title page-name uri]
  [:a {:class (str cls (if (= current-page page-name) " current" ""))
       :href uri
       :on-click (fn [e]
                   (.preventDefault e)
                   (nav! uri page-name))}
   title])

(rum/defc navigation-bar < rum.core/static
  [current-page]
  [:div {:id "nav-bar"}
   [:div {:class "rat-img"}
    [:div {:class "left rat2"}]]
   [:div {:id "nav-bar-items"}
    ;; logo with link to "/"
    (link "logo" current-page ".:CyRats:." :index "/")
    ;; other navigation bar links
    (for [[page-name [title uri]] NAVIGATION]
      (link "nav-item" current-page title page-name uri))]
   [:div {:class "rat-img"}
    [:div {:class "left rat1"}]]])

(rum/defc footer < rum.core/static
  []
  [:div {:id "footer"}
   [:div {:class "rat-img"}
    [:div {:class "left rat1"}]]
   [:div {:id "footer-items"}
    [:span {} "Copyright 2015, REPLicantus"]]
   [:div {:class "rat-img"}
    [:div {:class "right rat3"}]]])

(rum/defc sidebar < rum.core/static
  [page arenas]
  [:div {:id "side-bar"}
   [:ul
    (map (fn [[id title]]
           (link "nav-item" page title [:page id] (str "/arena/" id)))
         arenas)]])

;; frames ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn onclick-fn [event]
  (sockets/send-message :debug :payload)
  )

(rum/defc index-frame < rum.core/static
  [_ state]
  [:div {:class "index"}
   [:pre {} "Rust... Rust never changes. It creeps into your home, your things, your thoughts.
Even food tastes a little rusty. But who I am to complain?
At least I _have_ food, what little amount my CyberRats(tm) can scrounge from the surface.

And so it goes, Great Cycle Of Life:

Build. Wait. Eat.
Rinse. Repeat.
Die."]
   [:img {:src "/static/battleground.png"
          :onClick onclick-fn ;; stupid way of debugging sockets
          }]
   ])

(rum/defc stub-frame
  [_]
  [:p "This page under construction"])

;; root widget

(rum/defc Window < rum.core/reactive
  []
  (let [{:keys [page arenas] :as state} (rum/react STATE)]
    [:div {:id "root"}
     (navigation-bar page)
     [:div {:id "content"}
      [:div {:class "frame"}
       ((get {:index index-frame}
             page
             stub-frame) state)]
      (sidebar page arenas)]
     (footer)]))

(let [root (.getElementById js/document "react")
      comp (rum/mount (Window) root)]
  (defn refresh
    [state]
    (rum/request-render comp)))

(add-watch STATE nil (fn [_ _ _ state]
                       (refresh state)))

(refresh @STATE)
