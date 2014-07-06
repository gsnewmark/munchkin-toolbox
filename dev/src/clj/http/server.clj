;;; This namespace is used for development and testing purpose only.
(ns http.server
  (:require [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer (resources)]
            [compojure.core :refer (GET defroutes)]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.adapter.jetty :as jetty]
            [clojure.java.io :as io]))

(enlive/deftemplate page "index.html" []
  [:body] (enlive/append
           (enlive/html [:script {:src "js/goog/base.js" :type "text/javascript"}])
           (enlive/html [:script (slurp (io/resource "js/repl.js"))])))

(defroutes site
  (resources "/")
  (GET "/*" req (page)))

(def app
  (-> site
      (wrap-resource "META-INF/resources")))

(defn run
  "Run the ring server. It defines the server symbol with defonce."
  []
  (defonce server
    (jetty/run-jetty #'app {:port 3000 :join? false}))
  server)
