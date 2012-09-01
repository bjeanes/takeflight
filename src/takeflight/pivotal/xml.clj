(ns takeflight.pivotal.xml
  (:require [clojure.xml :as xml])
  (:import java.text.SimpleDateFormat))

(defn- date-parse [^String format ^String date]
  (.parse (java.text.SimpleDateFormat. format) date))

(defn- ->pt+dispatch
  [{{tag-type :type :as attrs} :attrs
    :keys [tag content]}]

  (or tag-type [(type (first content))]))

(defmulti ->pt #'->pt+dispatch)

(defmethod ->pt
  "array"
  [{:keys [tag content]}]

  {tag (vec (map ->pt (or content [])))})

(defmethod ->pt
  "integer"
  [{:keys [tag content]}]

  {tag (Integer/parseInt (first content))})

(defmethod ->pt
  "float"
  [{:keys [tag content]}]

  {tag (Float/parseFloat (first content))})

(defmethod ->pt
  "datetime"
  [{:keys [tag content]}]

  {tag (date-parse "y/M/d h:m:s z" (first content))})

(defmethod ->pt
  "date"
  [{:keys [tag content]}]

  {tag (date-parse "y/M/d" (first content))})

(defmethod ->pt
  "boolean"
  [{:keys [tag content]}]

  {tag (= "true" (first content))})

(defmethod ->pt
  [java.lang.String]
  [{:keys [tag content]}]

  {tag (apply str content)})

(defmethod ->pt
  [clojure.lang.PersistentStructMap]
  [{:keys [tag content]}]

  (let [attributes (map ->pt content)
        entity (apply merge attributes)]
    (if (= 1 (count attributes))
      {tag entity}
      entity)))

(defmethod ->pt :default [xml] xml)