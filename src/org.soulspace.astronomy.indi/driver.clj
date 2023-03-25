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

