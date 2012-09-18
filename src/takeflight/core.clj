(ns takeflight.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer :all]
            [takeflight.web :as web]))

(defn -main
  []
  (web/init)
  (run-jetty web/handler {:port 8080})
  (web/destroy))
