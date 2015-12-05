(ns user
  (:require [reloaded.repl :refer [system reset stop]]
            [cyrats.system]))

;;; 


(reloaded.repl/set-init! (fn [] (cyrats.system/create-system 8080)))
