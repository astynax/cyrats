(ns cyrats.utils)

(defn merge-module-stats
  [modules]
  ;; TODO try reduce
  (apply (partial merge-with +) modules))

(defn log-message [game message]
  (update-in game [:messages] (fn [v] (conj v message))))

(defn clear-messages
  [game]
  (update-in game [:messages] (fn [v] (vec []))))
