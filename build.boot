(task-options!
  pom {:project     'munchkin-toolbox
       :version     "0.3.3-SNAPSHOT"
       :description "A set of tools for the Munchkin game"
       :url         "https://gsnewmark.github.io/munchkin-toolbox"
       :scm         {:url "https://github.com/gsnewmark/munchkin-toolbox"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure       "1.7.0"]
                  [org.clojure/clojurescript "0.0-3308"]
                  [org.clojure/core.async    "0.1.346.0-17112a-alpha"]
                  [org.omcljs/om             "0.8.8"]
                  [sablono                   "0.3.4"]
                  [org.webjars/uikit         "2.20.3"]

                  [adzerk/boot-cljs      "0.0-3308-0"      :scope "test"]
                  [adzerk/boot-reload    "0.3.1"           :scope "test"]
                  [pandeiro/boot-http    "0.6.3-SNAPSHOT"  :scope "test"]])

(require
  '[adzerk.boot-cljs   :refer [cljs]]
  '[adzerk.boot-reload :refer [reload]]
  '[pandeiro.boot-http :refer [serve]])

(deftask none-opts "Set CLJS compiler options for development environment." []
  (task-options!
   cljs {:optimizations :none
         :source-map    true
         :compiler-options {}})
  identity)

(deftask advanced-opts "Set CLJS compiler options for production build." []
  (task-options!
   cljs {:optimizations    :advanced
         :compiler-options {:closure-defines {:goog.DEBUG false}
                            :elide-asserts   true}})
  identity)

(deftask dev "Start development environment." []
  (comp (none-opts)
        (serve :dir           "target/"
               :resource-root "META-INF/resources/")
        (watch)
        (speak)
        (reload :on-jsload 'munchkin-toolbox.core/start)
        (cljs)))

(deftask build "Start production build." []
  (comp (advanced-opts)
        (cljs)))
