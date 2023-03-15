(ns org.soulspace.astronomy.indi.driver
  (:require [clojure.core.async :as async]
            [indi4clj.protocol :as ip]))

;;;
;;; common INDI driver functions
;;;


;; device name
;; device connection

; property vectors ([group]/name, type)
;   properties (name/ type)

(defprotocol INDIDriver
  "Protocol for INDI drivers"
  (send-command [driver command] "Sends a device command to the out channel.")
  (handle-command [driver command] "Reads a client command from the in channel")
  )

(comment 
  (def state (atom {}))

  (def in (async/chan))
  (def out (async/chan))
)

