(ns takeflight.pivotal
  (:require [clj-http.client :as client]
            [clojure.xml :as xml]
            [clojure.walk :as walk])
  (:import [java.io ByteArrayInputStream]
           [java.text SimpleDateFormat]))

(defmethod client/coerce-response-body
  :xml
  [_ response]
  (update-in response [:body] #(-> % java.io.ByteArrayInputStream. xml/parse)))

(defn- project-url [id]
  (str "https://www.pivotaltracker.com/services/v4/projects/" id))

(defn- xml->stories 
  [xml]

  (let [prewalked (walk/prewalk
                   (fn [node]
                     (cond
                      (map? node) (let [tag (:tag node)
                                        attrs (:attrs node)
                                        type (:type attrs)
                                        content (:content node)
                                        content1 (first content)]
                                    (cond 
                                     (= type "array") {tag content}
                                     (= type "integer") [tag (Integer/parseInt content1)]
                                     (= type "datetime") [tag (.parse (java.text.SimpleDateFormat. "y/m/d H:m:s z") content1)]
                                     (or (nil? content) (= 1 (count content))) [tag content1]

                                     :else {tag content}))
                      :else node))
                   xml)
        postwalked (walk/postwalk 
                    (fn [node]
                      (cond
                       (:story node) (:story node)
                       (and (map? node) (nil? (:attrs node)) (:tag node)) {(:tag node) (:content node)}
                       (and (vector? node) (every? vector? node)) (apply (partial assoc {}) (flatten node))
                       (and (map? node) (= 1 (count node)) (map? (first (vals node)))) (first (vals node))
                       :else node)) 
                    prewalked)]
    (:stories postwalked)))

(defn stories
  ([project-id api-token] (stories project-id api-token nil))
  ([project-id api-token filter]
     (let [response (client/get (str (project-url project-id) "/stories")
                                {:query-params {:filter (str filter)}
                                 :headers {"X-TrackerToken" api-token}
                                 :as :xml})
           xml (:body response)]

       (xml->stories xml))))

(defn releases 
  [project-id api-token]
  (stories project-id api-token "type:release includedone:true"))