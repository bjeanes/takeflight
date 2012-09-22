(ns takeflight.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer :all]
            [takeflight.web :as web]))

(defn -main
  [& args]
  (let [arg1 (first args)
        port (if arg1
               (Integer/parseInt arg1)
               3000)]
    (web/init)
    (run-jetty web/handler {:port port})
    (web/destroy)))
