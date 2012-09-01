(ns takeflight.pivotal
  (:refer-clojure :exclude [get])
  (:require clojure.xml
            [takeflight.pivotal.xml :as xml]
            [clj-http.client :as client])
  (:import java.io.ByteArrayInputStream))

(def ^:private per-page 100)

(defn- project-url [id]
  (str "https://www.pivotaltracker.com/services/v4/projects/" id))

(defn- request
  [method api-token project-id path params]

  (method (str (project-url project-id) path)
              {:query-params params
               :headers {"X-TrackerToken" api-token}
               :as :pivotal-xml}))

(defn- get
  ([api-token project-id path] (get api-token project-id path {}))
  ([api-token project-id path params]
     (request client/get api-token project-id path params)))

(defn stories
  ([api-token project-id] (stories api-token project-id nil))
  ([api-token project-id filter] 
     (letfn [(get-page [page]
               (lazy-seq
                (let [offset (* per-page (dec page))
                      current-page (get-in (get api-token project-id "/stories"
                                                {:filter (str filter)
                                                 :limit per-page
                                                 :offset offset})
                                           [:body :stories])]
                  (when (not-empty current-page)
                      (concat current-page (get-page (inc page)))))))]

       (get-page 1))))

(defn releases 
  [api-token project-id]
  (stories api-token project-id "type:release includedone:true"))

(defn project
  [api-token project-id]
  (:body (get api-token project-id "/")))

