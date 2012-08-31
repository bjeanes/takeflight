(ns takeflight.pivotal
  (:refer-clojure :exclude [get])
  (:require clojure.xml
            [takeflight.pivotal.xml :as xml]
            [clj-http.client :as client])
  (:import java.io.ByteArrayInputStream))

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
  ([project-id api-token] (stories project-id api-token nil))
  ([project-id api-token filter]
     (let [response (get api-token project-id "/stories" {:filter (str filter)})
           body (:body response)]

       (:stories body))))

(defn releases 
  [project-id api-token]
  (stories project-id api-token "type:release includedone:true"))

(defn project
  [project-id api-token]
  (:body (get api-token project-id "/")))

