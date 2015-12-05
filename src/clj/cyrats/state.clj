(ns cyrats.state)

(def STATE (atom {:arenas []}))

(defn get-state [] @STATE)
