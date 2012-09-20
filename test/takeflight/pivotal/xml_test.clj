(ns takeflight.pivotal.xml-test
  (:require [clojure.test :refer :all]
            [takeflight.pivotal.xml :refer :all]))

(defn- xml-equiv?
  [xml-str data]

  (let [new-data (xml->pt (str->xml xml-str))]
    (or (= new-data data) (prn new-data))))

(deftest xml-conversions
  (testing "XML conversions"
    (are [xml result] (xml-equiv? xml result)

         ;; untyped (string)
         "<name>Foo</name>" {:name "Foo"}

         ;; integer
         "<id type=\"integer\">123</id>" {:id 123}

         ;; datetime
         "<created_at type=\"datetime\">
            2008/12/10 12:48:04 UTC
          </created_at>" {:created_at #inst "2008-12-10T12:48:04.000-00:00"}

         ;; simple entity
         "<story>
            <name>Foo</name>
            <label>bar</label>
          </story>" {:name "Foo" :label "bar"}

          ;; nested entity
          "<story>
            <name>Foo</name>
            <attachments type=\"array\">
              <attachment>
                <id type=\"integer\">4</id>
                <filename>shield_improvements.pdf</filename>
                <description>How to improve the shields in 3 easy steps.</description>
                <uploaded_by>James Kirk</uploaded_by>
                <uploaded_at type=\"datetime\">2008/12/10 00:00:00 UTC</uploaded_at>
                <url>http://www.pivotaltracker.com/resource/download/1295103</url>
              </attachment>
            </attachments>
          </story>" {:name "Foo"
                     :attachments [{:id 4
                                    :url "http://www.pivotaltracker.com/resource/download/1295103"
                                    :filename "shield_improvements.pdf"
                                    :uploaded_by "James Kirk"
                                    :uploaded_at #inst "2008-12-10T00:00:00.000-00:00"
                                    :description "How to improve the shields in 3 easy steps."}]}

         ;; empty array
         "<stories type=\"array\"></stories>" {:stories []}

         ;; array with contents
         "<stories type=\"array\">
            <story>
              <name>Foo</name>
              <label>bar</label>
            </story>
          </stories>" {:stories [{:name "Foo" :label "bar"}]})))
