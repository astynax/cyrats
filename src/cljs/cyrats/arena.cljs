(ns cyrats.arena
  (:require [rum.core :as rum]
            [cyrats.arena-model :as m]))

(defonce STATE
  (atom
   (m/->model [[[4 2 1]]]
              [[3 0 2]
               [0 4 1]
               [2 2 2]
               [1 0 4]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- stats->elems
  [{:keys [hp ap dp]}]
  (letfn [(item [cls v]
            [:span {:class (if (zero? v) "zero" cls)}
             v])]
    [(item "hp" hp)
     (item "ap" ap)
     (item "dp" dp)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(rum/defc module
  [m idx]
  (vec
   (concat [:div {:class "module"
                  :on-click
                  (fn [_] (swap! STATE m/use-module idx))}]
           (stats->elems m))))

(rum/defc rat-module
  [m rat-idx idx]
  (vec
   (concat [:div (if (nil? m)
                   {:class "empty"}
                   {:class "full"
                    :on-click
                    (fn [_]
                      (swap! STATE m/drop-module rat-idx idx))})]
           (if (nil? m) ["---"] (stats->elems m))
           )))

(rum/defc rat
  [[{:keys [modules]} selected?] idx]
  (let [module-count (count modules)]
    [:div {:class (if selected? "selected rat" "rat")
           :on-click
           (fn [_]
             (swap! STATE m/select-rat idx))}
     ;; stats
     (vec (concat [:div] (stats->elems (m/total-stats modules))))
     ;; space usage
     [:div {:class "cells"}
      (map (fn [m i] (rat-module m idx i))
           (concat modules (repeat nil))
           '(0 1 2))]]))

(rum/defc hangar < rum.core/reactive
  []
  (let [{:keys [rats storage selected-rat]} (rum/react STATE)]
    [:table
     [:tbody
      [:tr
       [:th {:id "th-rats"} "Rats"]
       [:th {:id "th-modules"} "Modules"]]
      [:tr
       [:td
        (map-indexed #(rat [%2 (= selected-rat %1)] %1)
                     rats)]
       [:td
        (map-indexed #(module %2 %1)
                     storage)
        ]]]]))
