(ns takeflight.time
  (:refer-clojure :exclude [extend])
  (:require [clj-time.core :as t]
            [clj-time.coerce :refer :all]
            [clojure.string :only [:replace] :as str])
  (:import org.ocpsoft.pretty.time.PrettyTime
           java.util.Date
           java.text.SimpleDateFormat))

(defn distance-of-time-in-words
  [^Date d1 ^Date d2]

  (.format (PrettyTime. d1) d2))

(defn before?
  [d1 d2]

  (t/before? (from-date d1) (from-date d2)))

(defn relative-time-for-status
  [& ds]

  (let [s (apply distance-of-time-in-words ds)]
    (-> s
        (str/replace "from now" "late")
        (str/replace "ago" "early"))))

(defn day-and-month
  [^Date date]

  (.format (SimpleDateFormat. "MMM d") date))