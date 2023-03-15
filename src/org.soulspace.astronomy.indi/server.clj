(ns org.soulspace.astronomy.indi.server
  (:require [clojure.java.io :as io]
            [org.soulspace.astronomy.indi.protocol :as ip])
  (:import [java.net ServerSocket]
           [java.io StringWriter]))

;;
;; Common INDI server functions
;;

(def state (atom {}))

(defn receive
  "Read a line of textual data from the given socket."
  [socket]
  (.readLine (io/reader socket)))

(defn send
  "Send the given string message out over the given socket."
  [socket msg]
  (let [writer (io/writer socket)]
    (.write writer msg)
    (.flush writer)))

(defn serve
  "Accepts and handles requests on the given port."
  [port handler]
  (with-open [server-sock (ServerSocket. port)
              sock (.accept server-sock)]
    (let [msg-in (receive sock)
          msg-out (handler msg-in)]
      (send sock msg-out))))

(defn start
  [& drivers]
  (doseq [driver drivers]))
    ;(init driver)

(defprotocol INDIServer
  "")

