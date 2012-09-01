(ns takeflight.pivotal
  (:refer-clojure :exclude [get])
  (:require clojure.xml
            [takeflight.pivotal.xml :as xml]
            [clj-time.core :as t]
            [clj-time.coerce :refer [to-date from-date]]
            [clj-http.client :as client])
  (:import java.io.ByteArrayInputStream))

(def ^:private per-page 100)

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

(defn project
  [api-token project-id]
  (:body (get api-token project-id "/")))

(defn stories
  ([api-token project-id] (stories api-token project-id nil))
  ([api-token project-id filter] 
     (letfn [(get-page [page]
               (lazy-seq
                (let [offset (* per-page (dec page))
                      current-page (get-in (get api-token project-id "/stories"
                                                {:filter (str filter)
                                                 :limit per-page
                                                 :offset offset})
                                           [:body :stories])]
                  (when (not-empty current-page)
                      (concat current-page (get-page (inc page)))))))]

       (get-page 1))))

(defn backlog+current
  [api-token project-id]
  ;; all states except unscheduled (icebox):
  (stories api-token project-id "includedone:false state:unstarted,started,finished,delivered,accepted,rejected"))

(defn releases 
  [api-token project-id]
  (stories api-token project-id "type:release includedone:true"))

(defn releases+projections
  [api-token project-id]
  (let [project (project api-token project-id)
        stories (backlog+current api-token project-id)
        {velocity :current_velocity
         project-start :first_iteration_start_time
         weeks-per-iteration :iteration_length
         iteration :current_iteration_number} project
        points-per-week (/ velocity weeks-per-iteration)
        days-per-point (/ 7 points-per-week)
        iteration-start (t/plus (from-date project-start)
                                (t/weeks (* weeks-per-iteration iteration)))]

    ;; TODO:
    ;; iterate over `stories`, adding up the points until we hit a
    ;; release. when we hit a release, multiply the number of points
    ;; by days-per-point to get the distance between iteration-start
    ;; and the expected delivery of the given release. Add that many
    ;; days to iteration-start to get expected-delivery-date and merge
    ;; that into the release map and emit it in the resulting sequence

    ;; FIXME: This is giving a very different date than PT
    (letfn [(r [{:keys [points releases] :as accum}
                {type :story_type estimate :estimate :as story}]
              (cond
               points (update-in accum [:points] + estimate)
               (= "release" type) (-> accum
                                      (assoc :points 0.0)
                                      (update-in [:releases] conj
                                                 (assoc story
                                                   :eta (to-date (t/plus iteration-start
                                                                         (t/weeks (* days-per-point points)))))))
               :else accum))]
      (:releases (reduce r {:points 0.0 :releases []} stories)))))


