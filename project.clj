(defproject cyrats "0.1.0-SNAPSHOT"
  :description "CyRats game"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :profiles
  {:repl
   {:dependencies [[org.clojure/tools.nrepl "0.2.12"]
                   [reloaded.repl "0.1.0"]]
    :source-paths ["dev" "test/clj"]
    }

   ;; commons for server-side
   :server-commons
   {:dependencies [[http-kit "2.1.19"]
                   [compojure "1.4.0"]
                   [com.stuartsierra/component "0.3.0"]
                   [sonian/carica "1.2.1" :exclusions [[cheshire]]]]
    :source-paths ["src/clj"
                   "src/cljc"]
    :main cyrats.system
    }

   ;; dev customizations
   :dev-overrides
   {:jvm-opts ["-Xmx256m"]
    :resource-paths ["resources"
                     "target/classes"]
    }

   ;; server customizations
   :server-overrides
   {:jvm-opts ["-Xmx1g"
               "-server"]
    :aot [cyrats.system]
    }

   ;; compound profiles (don't edit, use -commons and -overrides instead)
   :dev [:server-commons :dev-overrides]
   :server [:server-commons :server-overrides]

   ;; profile for unit-testing
   :test
   {:plugins [[lein-auto "0.1.2"]]
    :jvm-opts ["-Xmx256m"]
    :test-paths ["test/clj"]
    :auto {:default {:file-pattern #"\.(clj|cljc)$"}}
    }

   ;; profile for client-side developing
   :cljs
   {:jvm-opts ["-Xmx1g"]
    :dependencies [[org.clojure/clojurescript "1.7.170"]
                   [quiescent "0.2.0-RC2"]]
    :plugins [[lein-cljsbuild "1.1.1"]
              [lein-figwheel "0.5.0-2"]]

    :resource-paths ["resources"
                     "target/classes"]
    :cljsbuild
    {:builds {:dev
              {:figwheel true
               :source-paths ["src/cljc"
                              "src/cljs"]
               :compiler {:output-to "target/classes/public/main.js"
                          :output-dir "target/classes/public/out"
                          :asset-path "out"
                          :main "cyrats.core"
                          :optimization :none
                          :recompile-dependents true}}
              :prod
              {:source-paths ["src/cljc"
                              "src/cljs"]
               :compiler {:output-to "resources/public/main.js"
                          :optimizations :simple
                          :pretty-print false}}}
     }}
   }

  :figwheel
  {:css-dirs ["resources/public"]}

  :aliases
  {;; launches figwheel
   "fw" ["with-profile" "repl,cljs" "figwheel"]
   ;; generates a prod version of the JS-library
   "prod" ["with-profile" "cljs" "cljsbuild" "once" "prod"]
   ;; launches autorunner for unit-tests
   "autotest" ["with-profile" "dev,test" "auto" "test"]
   
   }
  )
