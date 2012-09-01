(ns takeflight.web
  (:require [compojure.core :refer :all]
            [takeflight.pivotal :as pt]))

(def api-token "replace-me")

(def webapp
  (routes

   (GET "/:id"
        [id]
        (if id

          (str "<ol>"
      	       (apply str (map 
                           #(str "<li>" 
                                 (:name %) 
                                 " - "
                                 (:deadline %)
                                 "</li>") 
                           (filter :deadline (pt/releases api-token id))))
               "</ol>")
          "Bad ID"))))