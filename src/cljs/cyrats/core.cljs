(ns cyrats.core
  (:require [rum.core :as rum]
            [goog.events]
            [cyrats.arena :refer [hangar]])
  (:import [goog.history Html5History EventType]))

(defonce STATE (atom {:page :index
                      :arenas [[1 "Arena 1"]]}))

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
   ;;[:profile ["[ Profile ]" "/profile"]]
   ])

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
           (link "nav-item" page title [:arena id] (str "/arena/" id)))
         arenas)]])

;; frames ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(rum/defc index-frame < rum.core/static
  [_]
  [:div {:class "index"}
   [:p "Rust... Rust never changes. It creeps into your home, your things, your thoughts."]
   [:p "Even food tastes a little rusty. But who I am to complain?"]
   [:p "At least I _have_ food, what little amount my CyberRats(tm) can scrounge from the surface."]
   [:br]
   [:p "And so it goes, Great Cycle Of Life:"]
   [:p "Build. Wait. Eat."]
   [:p "Rinse. Repeat."]
   [:p "Die."]
   [:img {:src "/static/battleground.png"}]
   ])

(rum/defc about-frame < rum.core/static
  [_]
  [:div {:class "about"}
   [:p "Basically, it comes down to this: battles between custom CyberRats(tm) for last scraps of resources from surface.
You may ask: 'Why not use rats? Humans are cheaper! Put one in EVA suit and get his lazy ass to work for living.'
I wish it was that simple. Earth's surface is so thoroughly irradiated and polluted, that no amount of shielding will prevent
you from turning into rad-ghost or, Heaven forbid, mutant scum."]
   [:p "Enough with the flavour, here's the basics:"]
   [:ul
    [:li "You equip one or more CyberRats(tm) with CyberBlocks(tm). Each of those adds points to three attributes: Health, Attack and Defence."]
    [:li "2. Attributes are summed up by category and Rat is ready to go!"]
    [:li "3. You send one of the fully equipped CyberRats to the Surface. There it will battle other CyberRats over scraps of food and if
victorious - bring them home."]
    [:li "4. Repeat till the end of game session."]
    [:li "5. If you have food by the end of session - you won!"]]
   [:p "Amount of food in your shelter decreases by random amount at random intervals. Life is harsh, I know."]
   [:p "Now, gear up and go! Let the scavenging begin!"]
   ])

(rum/defc arena-frame
  [_]
  [:div {:class "arena"}
   [:div {:class "session-log"}]
   [:div {:class "hangar"}
    (hangar)]])

(rum/defc stub-frame
  [_]
  [:p "This page is under construction"])

;; root widget

(rum/defc Window < rum.core/reactive
  []
  (let [{:keys [page arenas] :as state} (rum/react STATE)]
    [:div {:id "root"}
     (navigation-bar page)
     [:div {:id "content"}
      [:div {:class "frame"}
       ((get {:index index-frame
              :about about-frame
              [:arena 1] arena-frame}
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
