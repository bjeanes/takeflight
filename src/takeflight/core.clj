(ns takeflight.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer :all]
            [takeflight.web :refer :all]))

(defn -main
  []
  (run-jetty webapp {:port 8080}))
