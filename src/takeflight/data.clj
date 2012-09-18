(ns takeflight.data
  (:require [takeflight.pivotal :as pt]))

(defonce ^:private milestones-by-project (atom {}))
(defonce ^:private projects-to-fetch (agent #{}))

(defn milestones
  []

  (let [milestones (apply concat (vals @milestones-by-project))]
    (sort-by :eta milestones)))

(defn update-project-list!
  [api-token]

  (let [projects (pt/projects api-token)
        project-ids (map :id projects)]
    (send projects-to-fetch into project-ids)))

;; TODO: make this idempotent otherwise multiple calls will start
;; multiple scheduler threads
(defn start-fetchers
  [api-token & {:keys [log?] :or {:log? false}}]

  (let [log #(when log? (println (apply str %&)))]
    (log "Starting fetchers...")

    (future
      (let [minute-in-ms (* 60 1000)
            project-fetch-time-in-ms (* 60 minute-in-ms)
            milestone-fetch-time-in-ms (* 5 minute-in-ms)]

        (log "Updating project list...")
        (update-project-list! api-token)

        (future
          ((fn []
             (Thread/sleep project-fetch-time-in-ms)
             (log "Updating project list...")
             (update-project-list! api-token)
             (recur))))

        ((fn []
           (doseq [id @projects-to-fetch]
             (future
               (log "Fetching project " id)
               (let [milestones (pt/releases+projections api-token id)]
                 (swap!
                  milestones-by-project
                  #(assoc % id milestones)))))
           (Thread/sleep milestone-fetch-time-in-ms)
           (recur)))))))