(ns takeflight.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as enlive]
            [takeflight.pivotal :as pt]
            [takeflight.time :refer :all]
            [ring.middleware.reload :as ring]))

(def ^:private api-token (System/getenv "TOKEN"))

(enlive/deftemplate not-found "public/404.html" [])

(enlive/deftemplate layout "views/layout.html"
  [title body]

  [:head :title] (enlive/append (when title (str " / " title)))
  [:body :> :h1] (enlive/after (when title (enlive/html-snippet (str "<h2>" title "</h2>"))))
  [:div#content] (enlive/content body))

(defn- decorate-release-for-display
  [{:keys [name eta deadline project] :as release}]

  (let [name (str name)
        status (relative-time-for-status deadline eta)
        eta (day-and-month eta)
        deadline (day-and-month deadline)
        status (if (= eta deadline) "Scheduled" status)
        project-name (:name project)]

    {:status status
     :name name
     :eta eta
     :deadline deadline
     :project-name project-name}))

(enlive/defsnippet flight-status-board "views/status-board.html" [:table#status-board]
  [releases]

  [:tbody :tr] (enlive/clone-for
                [{:keys [name eta deadline status project-name]}
                 (map decorate-release-for-display releases)]

                [:.from] (enlive/content project-name)
                [:.to] (enlive/content name)
                [:.arrives] (enlive/content eta)
                [:.scheduled] (enlive/content deadline)
                [:.status] (enlive/content status)))

(def ^:private handler
  (routes

   (GET "/"
        []

        (layout nil (enlive/html-content "<a href=\"/584807\">Quantum Lead</a>")))

   (GET "/:id"
        [id]

        (layout "Project Overview"
                (if id
                  (flight-status-board (pt/releases+projections api-token id))
                  (enlive/content "Bad ID"))))

   (route/resources "/")
   (route/files "/")
   (route/not-found (not-found))))

(def webapp (ring/wrap-reload #'handler '(takeflight.web )))