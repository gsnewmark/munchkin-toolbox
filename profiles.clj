{:shared {:cljsbuild {:builds {:munchkin-toolbox {:source-paths ["src/cljs"]
                                          :compiler {:preamble ["react/react.min.js"]
                                                     :externs ["react/externs/react.js"]}}}}}
 :test [:shared
        {:cljsbuild {:builds {:munchkin-toolbox {:compiler {:optimizations :whitespace
                                                    :pretty-print true}}}}}]
 :dev [:shared
       {:source-paths ["dev/src/clj"]
        :resource-paths ["dev/resources" "target/cljsbuild" "dev/src"]
        :dependencies [[ring "1.2.1"]
                       [compojure "1.1.6"]
                       [enlive "1.1.5"]
                       [com.cemerick/piggieback "0.1.3"]
                       [weasel "0.2.0"]]
        :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
        :cljsbuild {:builds {:munchkin-toolbox {:source-paths ["dev/src/cljs/repl"]
                                        :compiler {:output-dir "target/cljsbuild/public/js"
                                                   :output-to "target/cljsbuild/public/js/munchkin-toolbox.js"
                                                   :source-map "target/cljsbuild/public/js/munchkin-toolbox.js.map"
                                                   :optimizations :whitespace
                                                   :pretty-print true}}}}}]
 :prod [:shared
        {:cljsbuild {:builds {:munchkin-toolbox {:compiler {:output-to "resources/js/munchkin-toolbox.js"
                                                    :optimizations :advanced
                                                    :pretty-print false}}}}}]}
