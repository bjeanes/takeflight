(ns takeflight.pivotal.xml-test
  (:require [clojure.test :refer :all]
            [takeflight.pivotal.xml :refer :all]))

(deftest xml-conversions
  (testing "XML conversions"
    (are [xml result]
         (= (xml->pt (str->xml xml)) result)

         ;; untyped (string)
         "<name>Foo</name>" {:name "Foo"}

         ;; integer
         "<id type=\"integer\">123</id>" {:id 123}

         ;; entity
         "<story>
            <name>Foo</name>
            <label>bar</label>
          </story>" {:name "Foo" :label "bar"}

         ;; empty array
         "<stories type=\"array\"></stories>" {:stories []}

         ;; array with contents
         "<stories type=\"array\">
            <story>
              <name>Foo</name>
              <label>bar</label>
            </story>
          </stories>" {:stories [{:name "Foo" :label "bar"}]})))
