;;;;
;;;;   Copyright (c) Ludger Solbach. All rights reserved.
;;;;
;;;;   The use and distribution terms for this software are covered by the
;;;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;;;   which can be found in the file license.txt at the root of this distribution.
;;;;   By using this software in any fashion, you are agreeing to be bound by
;;;;   the terms of this license.
;;;;
;;;;   You must not remove this notice, or any other, from this software.
;;;;

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
  ""
  )

