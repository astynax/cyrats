(ns cyrats.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [cyrats.model :as m]))

(defonce STATE (atom 42))

(q/defcomponent View [state]
  (d/h1 {} state))

(let [root-el (.getElementById js/document "root")]
  (defn- redraw [state]
    (q/render (View state) root-el)))

(add-watch STATE nil (fn [_ _ _ state]
                       (redraw state)))

(redraw @STATE)

