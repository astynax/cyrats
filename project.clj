(defproject cyrats "0.1.0-SNAPSHOT"
  :description "CyRats game"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]

  :profiles
  {:repl {:dependencies [[org.clojure/tools.nrepl "0.2.12"]]}

   :clj {:jvm-opts ["-Xmx512m"]
         :source-paths ["src/cljc"]
         :test-paths ["test/clj"]
         :auto {:default {:file-pattern #"\.(clj|cljs|cljc)$"}}
         }

   :cljs {:jvm-opts ["-Xmx1g"]
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
                     :compiler {:output-to "target/classes/public/app.js"
                                :output-dir "target/classes/public/out"
                                :asset-path "out"
                                :main "cyrats.core"
                                :optimization :none
                                :recompile-dependents true}}
                    :prod
                    {:source-paths ["src/cljc"
                                    "src/cljs"]
                     :compiler {:output-to "target/classes/public/main.js"
                                :main "cyrats.core"
                                :optimizations :advanced
                                :pretty-print false}}}
           }}
   }

  :figwheel {:css-dirs ["resources/public"]}

  :aliases {"fw" ["with-profile" "user,cljs" "figwheel"]
            "prod" ["with-profile" "cljs" "cljsbuild" "once" "prod"]
            "autotest" ["with-profile" "clj" "auto" "test"]}
  )
