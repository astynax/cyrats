(ns cyrats.player
  (:require
   [cyrats.rat :as rat]
   [clojure.set :as set]))

(defn ->player [name food energy]
  {:modules #{}
   :food food
   :name name
   :energy energy})

(defn tick [player]
  (-> player
   (update :food - 10)
   (update :energy + 10)))

(defn is-alive? [{food :food}]
  (pos? food))

(defn can-assemble-rat?
  [{player-modules :modules} rat-modules]
  (and
   (= 3 (count rat-modules))
   (set/subset? rat-modules player-modules)))

(defn assemble-rat
  [player modules]
    (assoc
     (update-in player [:rats] conj (rat/->rat player modules))
     :modules (set/difference (:modules player) modules)))

(defn can-send-rat-to-arena?
  [player rat]
  (and
   (>= (:energy player) (rat/energy-required rat))
   (contains? (:rats player) rat)))

(defn send-rat-to-arena
  [player rat])
