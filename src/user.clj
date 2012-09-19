(require '[takeflight.pivotal.xml :as xml]
         '[clj-http.client :as http])

(defmethod http/coerce-response-body
  :xml
  [_ response]
  (update-in response [:body] #(-> % slurp xml/str->xml)))

(defmethod http/coerce-response-body
  :pivotal-xml
  [_ response]
  (update-in (http/coerce-response-body :xml response)
             [:body]
             xml/xml->pt))
