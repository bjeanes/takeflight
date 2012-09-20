(ns takeflight.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [takeflight.data :as data]
            [takeflight.web.views :as views]))

;; TODO: figure out a nice way to get this right
(def ^:private development? true)
(def ^:private api-token (System/getenv "TOKEN"))

(defroutes handler
  (GET "/" [] (views/layout (views/flight-status-board (data/milestones))))
  (route/resources "/")
  (when development? (route/resources "/" {:root "views"}))
  (route/not-found (views/not-found)))

(defn init [] (data/start-fetchers api-token))
(defn destroy [] (shutdown-agents))
