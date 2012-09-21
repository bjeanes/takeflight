(ns takeflight.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer :all]
            [takeflight.web :as web]))

(defn -main
  [& args]
  (let [port (or (first args) 3000)
        port (Integer/parseInt port)]
    (web/init)
    (run-jetty web/handler {:port port})
    (web/destroy)))
