(ns takeflight.pivotal
  (:require clojure.xml
            [takeflight.pivotal.xml :as xml]
            [clj-time.core :as t]
            [clj-time.coerce :refer [to-date from-date]]
            [clj-http.client :as client])
  (:import java.io.ByteArrayInputStream))

(def ^:private per-page 100)

(def ^:private api-url "https://www.pivotaltracker.com/services/v4")

(defn- project-url [id]
  (str api-url "/projects/" id))

(defn- request
  ([method api-token project-id path]
     (request method api-token project-id path {}))

  ([method api-token project-id path params]
     (method (str (project-url project-id) path)
             {:query-params params
              :headers {"X-TrackerToken" api-token}
              :as :pivotal-xml})))

(defn projects
  [api-token]
  (:projects (:body (request client/get api-token nil "/"))))

(defn project
  [api-token project-id]
  (:body (request client/get api-token project-id "/")))

(defn uncompleted-iterations
  [api-token project-id]

  (get-in (request client/get api-token project-id "/iterations/current_backlog")
          [:body :iterations]))

(defn stories
  ([api-token project-id] (stories api-token project-id nil))
  ([api-token project-id filter]
     (letfn [(get-page [page]
               (lazy-seq
                (let [offset (* per-page (dec page))
                      current-page (get-in
                                    (request client/get
                                             api-token
                                             project-id
                                             "/stories"
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

(defn- eta-calculator
  [weeks-per-iteration
   velocity
   iteration-start-date
   previous-points]

  (let [days-per-iteration (* 7 weeks-per-iteration)
        average-days-per-point (/ days-per-iteration velocity)
        days-till-eta (Math/round (* average-days-per-point (float previous-points)))]

    (to-date (t/plus (from-date iteration-start-date)
                     (t/days days-till-eta)))))

(defn- eta-annotator
  [{velocity :current_velocity
    weeks-per-iteration :iteration_length}]

  (fn [{:keys [points releases] :as accum}
      {type :story_type
       deadline :deadline
       {iteration-start :start iteration :number} :iteration
       :as story}]

    ;; reset iteration points when iteration changes
    (let [accum (if (not= iteration (:iteration accum))
                  (assoc accum :points 0 :iteration iteration)
                  accum)
          estimate (:estimate story 0)]

      (cond
       ;; estimated stories get added to the iteration points
       (> estimate 0) (update-in accum [:points] + estimate)

       ;; Things with deadlines (currently only releases) have an
       ;; ETA calculated based on the previous points and velocity
       deadline (let [eta (or (:accepted_at story)
                              (eta-calculator
                               weeks-per-iteration
                               velocity
                               iteration-start
                               (or points 0)))
                      release (assoc story :eta eta)]
                  (update-in accum [:releases] conj release))

       :else accum))))

(defn releases+projections
  [api-token project-id]

  (let [project (project api-token project-id)
        iterations (uncompleted-iterations api-token project-id)
        stories (stories-from-iterations iterations)
        cleanup #(-> %
                     (dissoc :iteration)
                     (assoc :project project))]

    (map cleanup
         (:releases
          (reduce (eta-annotator project)
                  {:releases []}
                  stories)))))
