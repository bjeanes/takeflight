(ns takeflight.data
  (:require [takeflight.pivotal :as pt]
            [takeflight.util :refer :all]))

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

(defn fetch-milestones!
  [api-token projects-ref milestones-ref]

  (doseq [id @projects-ref]
    (future
      (let [milestones (pt/releases+projections api-token id)]
        (swap! milestones-ref
               #(assoc % id milestones))))))

(defn-memo start-fetchers
  [api-token]

  (future
    (let [minutes (partial * 60 1000)]
      (tick-now (minutes 60)
                update-project-list!
                api-token)

      (tick-now (minutes 5)
                fetch-milestones!
                api-token
                projects-to-fetch
                milestones-by-project))))