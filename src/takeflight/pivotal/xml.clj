(ns takeflight.pivotal.xml
  (:require [clojure.xml :as xml])
  (:import java.text.SimpleDateFormat))


(defn- date-parse [^String format ^String date]
  (.parse (java.text.SimpleDateFormat. format) date))

(defn- xml->pt+dispatch
  [{{tag-type :type :as attrs} :attrs
    :keys [tag content]}]

  (or tag-type [(type (first content))]))

(def str->xml
  #(-> (str %) .getBytes java.io.ByteArrayInputStream. xml/parse))

(defmulti xml->pt #'xml->pt+dispatch)

(defmethod xml->pt
  "array"
  [{:keys [tag content]}]

  {tag (vec (map xml->pt (or content [])))})

(defmethod xml->pt
  "integer"
  [{:keys [tag content]}]

  {tag (Integer/parseInt (.trim (first content)))})

(defmethod xml->pt
  "float"
  [{:keys [tag content]}]

  {tag (Float/parseFloat (.trim (first content)))})

(defmethod xml->pt
  "datetime"
  [{:keys [tag content]}]

  {tag (date-parse "y/M/d H:m:s z" (.trim (first content)))})

(defmethod xml->pt
  "date"
  [{:keys [tag content]}]

  {tag (date-parse "y/M/d" (.trim (first content)))})

(defmethod xml->pt
  "boolean"
  [{:keys [tag content]}]

  {tag (= "true" (.trim (first content)))})

(defmethod xml->pt
  [java.lang.String]
  [{:keys [tag content]}]

  {tag (apply str content)})

(defmethod xml->pt
  [clojure.lang.PersistentStructMap]
  [{:keys [tag content]}]

  (let [attributes (map xml->pt content)
        entity (apply merge attributes)]
    (if (= 1 (count attributes))
      {tag entity}
      entity)))

(defmethod xml->pt :default [xml] xml)