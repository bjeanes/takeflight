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

(defn uncompleted-iterations
  [api-token project-id]

  (get-in (get api-token project-id "/iterations/current_backlog")
          [:body :iterations]))

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
  (stories api-token project-id "includedone:false state:unstarted,started,delivered,accepted,rejected"))

(defn unfinished-stories
  [api-token project-id]
  ;; all states except unscheduled (icebox):
  (stories api-token project-id "includedone:false state:unstarted,started"))

(defn releases
  [api-token project-id]
  (stories api-token project-id "type:release includedone:true"))

(defn- stories-from-iterations
  [iterations]
  (for [{:keys [stories] :as iteration} iterations
        story stories
        :let [iteration (dissoc iteration :stories)]]
    (assoc story :iteration iteration)))

;; TODO: refactor the crap out of this!
(defn releases+projections
  [api-token project-id]

  (let [{velocity :current_velocity
         weeks-per-iteration :iteration_length
         current-iteration-number :current_iteration_number
         :as project} (project api-token project-id)
        days-per-iteration (* 7 weeks-per-iteration)
        average-days-per-point (/ velocity days-per-iteration)
        iterations (uncompleted-iterations api-token project-id)
        stories (stories-from-iterations iterations)

        calc-etas (fn [{releases :releases :as accum}
                      {type :story_type
                       estimate :estimate
                       {start :start iteration :number} :iteration
                       :as story}]

                    (let [start (from-date start)
                          ;; reset points at each new iteration
                          accum (if (not= iteration (:iteration accum))
                                  (assoc accum :points 0 :iteration iteration)
                                  accum)
                          points (:points accum)
                          days-till-eta (if (zero? points) 0 (/ average-days-per-point points))
                          eta (t/plus start (t/days days-till-eta))
                          eta (to-date eta)]

                      (cond
                       (and estimate (> estimate 0)) (update-in accum [:points] + estimate)
                       (and (= "release" type)
                            (:deadline story)) (update-in accum [:releases] conj
                                                          (-> story
                                                              (dissoc :iteration)
                                                              (assoc :eta eta)))
                       :else accum)))]

    (map #(assoc % :project project)
         (:releases (reduce calc-etas
                            {:points 0 :releases [] :iteration current-iteration-number}
                            stories)))))
