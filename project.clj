(defproject takeflight "0.1.0-SNAPSHOT"
  :description "'Flight Status' dashboard for Pivotal Tracker releases"
  :url "https://github.com/bjeanes/takeflight"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.5.3"]
                 [clj-time "0.4.3"]
                 [enlive "1.0.1"]
                 [joda-time "2.1"]
                 [org.ocpsoft.prettytime/prettytime "1.0.8.Final"]
                 [ring/ring-jetty-adapter "1.1.5"]
                 [compojure "1.1.1"]]
  :main takeflight.core
  :ring {:handler takeflight.web/handler
         :init takeflight.web/init
         :destroy takeflight.web/destroy})
