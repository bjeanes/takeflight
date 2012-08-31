(require 'clojure.xml
         '[takeflight.pivotal.xml :as xml]
         '[clj-http.client :as http])

(defmethod http/coerce-response-body
  :xml
  [_ response]
  (update-in response [:body] #(-> % java.io.ByteArrayInputStream. clojure.xml/parse)))

(defmethod http/coerce-response-body
  :pivotal-xml
  [_ response]
  (update-in (http/coerce-response-body :xml response) [:body] xml/->pt))
