(ns cyrats.tick
  (:require
   [cyrats.game :as game ]))

(defonce games [])

(defn ->game [players]
   {:players players
    :tick 0
    :running? true})

(defn game-register [game games]
  (conj games game))

(defn game-can-tick [game]
  (> 1 (count (filter game/player-is-alive? (:players game)))))


(defn tick [game]
  (if (game-can-tick game)
    (assoc game :running? false)
    (-> game
        (update :tick inc)
        (process-arena)
        (process-rats))))


(defn tick-loop [games]
  (->> games)
  (map tick)
  (filter game-can-tick))

(def process-user-input [game msg]
  (case (:type msg)
    :rat ()
    :send-to-arena ()
    game))
