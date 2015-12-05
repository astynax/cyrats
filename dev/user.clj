(ns user
  (:require [reloaded.repl :refer [system reset stop]]
            [carica.core :refer [config]]
            [cyrats.system]))

;;; 


(reloaded.repl/set-init! (fn [] (cyrats.system/create-system
                                 ((config :server) :port))))
