(ns takeflight.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [net.cgrand.enlive-html :as h]
            [takeflight.pivotal :as pt]
            [takeflight.time :refer :all]))

;; TODO: figure out a nice way to get this right
(def ^:private development? true)
(def ^:private api-token (System/getenv "TOKEN"))
(def ^:private template "views/layout.html")

(defn- decorate-release-for-display
  [{:keys [name eta deadline project] :as release}]

  (let [name (str name)
        status (relative-time-for-status deadline eta)
        status-class (if (before? eta deadline) "early" "late")
        eta (day-and-month eta)
        deadline (day-and-month deadline)
        on-time (= eta deadline)
        status (if on-time "On Time" status)
        status-class (if on-time "on-time" status-class)
        project-name (:name project)]

    {:status status
     :status-class status-class
     :name name
     :eta eta
     :deadline deadline
     :project-name project-name}))

(h/deftemplate not-found "public/404.html" [])

(h/deftemplate layout template
  [body]

  [:div#content] body)

(h/defsnippet flight-status-board template [:table#status-board]
  [releases]

  ;; remove all rows except the first
  [:tbody [:tr (h/but h/first-child)]] (h/substitute nil)

  ;; duplicate the first row for each release
  [:tbody :tr]
   (h/clone-for
    [{:keys [name eta deadline status status-class project-name]}
     (map decorate-release-for-display releases)]

    [:.from] (h/content project-name)
    [:.to] (h/content name)
    [:.arrives] (h/content eta)
    [:.scheduled] (h/content deadline)
    [:.status] (h/do->
                (h/add-class status-class)
                (h/content status))))


(def webapp
  (routes
   (GET "/" []
     (layout (h/html-content "<a href=\"/584807\">Quantum Lead</a>")))

   (GET ["/:id" :id #"[0-9]+"] [id]
     (layout (h/content (if id
                          (flight-status-board (pt/releases+projections api-token id))
                          "Bad ID"))))

   (route/resources "/")
   (when development? (route/resources "/" {:root "views"}))
   (route/not-found (not-found))))
