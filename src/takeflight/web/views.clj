(ns takeflight.web.views
  (:require [takeflight.time :refer :all]
            [net.cgrand.enlive-html :as h]))

(def ^:private template "views/layout.html")

(defn- decorate-release-for-display
  [{:keys [name current_state eta deadline project] :as release}]

  (let [name (str name)
        status (relative-time-for-status deadline eta)
        status-class (if (before? eta deadline) "early" "late")
        eta (day-and-month eta)
        deadline (day-and-month deadline)
        on-time (= eta deadline)
        landed (= current_state "accepted")
        status (if on-time "On Time" status)
        status-class (if on-time "on-time" status-class)
        status (if landed "Landed" status)
        status-class (if landed "landed" status-class)
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

  [:div#content] (h/content body)
  [:head [:meta h/last-of-type]] (h/after
                                  {:tag :meta
                                   :attrs {:http-equiv "refresh"
                                           :content 5}}))

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