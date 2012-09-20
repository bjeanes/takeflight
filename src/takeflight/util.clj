(ns takeflight.util)

(defn tick
  "Call f with args every ms. First call will be after ms"
  [ms f & args]

  (future
    (doseq [f (repeatedly #(apply f args))]
      (Thread/sleep ms)
      (f))))

(defn tick-now
  "Call f with args every ms. First call will be immediately (and blocking)"
  [ms f & args]

  (apply f args)
  (apply tick ms f args))
