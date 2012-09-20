(ns takeflight.util
  (:import [java.util.concurrent
            ScheduledThreadPoolExecutor
            TimeUnit]))

(def ^:private ticker-pool
  (delay (ScheduledThreadPoolExecutor. 1)))

(defn shutdown-tickers []
  (when (realized? ticker-pool) (.shutdown @ticker-pool)))

(defn tick
  "Call f with args every ms. First call will be after ms"
  [ms f & args]

  (.scheduleWithFixedDelay @ticker-pool
                           #(apply f args)
                           ms ms
                           TimeUnit/MILLISECONDS))

(defn tick-now
  "Call f with args every ms. First call will be immediately (and blocking)"
  [ms f & args]

  (.get (.submit @ticker-pool #(apply f args)))
  (apply tick ms f args))
