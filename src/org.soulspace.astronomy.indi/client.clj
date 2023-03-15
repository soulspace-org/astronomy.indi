(ns org.soulspace.astronomy.indi.client
  (:require [org.soulspace.astronomy.indi.protocol :as ip]
            [org.soulspace.astronomy.indi.network :as inet]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component])
  (:import [java.net Socket]
           [java.io StringWriter]))

;;;
;;;Common INDI client functions
;;;

(def state (atom {:devices {}}))

(defn name?
  [name]
  (fn [m] (= (:name m) name)))

(defn add-device
  [device]
  (swap! state assoc :devices (merge (:devices @state) {(:name device) device})))

(defn add-property
  [p]
  (if-let [device (ic/device (:device p))]
    ;(swap! state )
    ))



(defn handle-def-type-vector
  [cmd])

(defn handle-set-type-vector
  [cmd])

(defn handle-del-property
  [cmd])

(defn handle-get-properties
  [cmd])


;; TODO check URLConnection with custom protocol

(defn send-command
  "Sends an request to the specified host, port, and path"
  [host port request]
  (with-open [sock (Socket. host port)
              writer (io/writer sock)
              reader (io/reader sock)
              response (StringWriter.)]
    (.append writer request)
    (.flush writer)
    (ip/parse-xml reader))) ; just reads the first response
    ;(io/copy reader response) ; blocks, because the connection is not closed by server
    ;(str response)))

    
(defn send-cmd
  ""
  [uri request]
  (let [url (inet/new-url uri)
        conn (.openConnection url)
        reader (io/reader (.getInputStream conn))
        writer (io/writer (.getOutputStream conn))]
    (println (type writer))
    (println (type reader))
    (.append writer request)
    (.flush writer)
    (let [result (ip/parse-xml reader)]  ; just reads the first response
 ;     (inet/close-url-connection conn)
      result)))
;(println (send-request "192.168.0.171" 7624 "<getProperties/>"))

(defprotocol INDIClient
  "Protocol for an INDI client"
  (send-command [client message] ""))

(defrecord INDIClientImpl [uri host port connection reader writer]
  component/Lifecycle
  (start [this]
    ; TODO are reader/writer needed in global state?
    (let [url (inet/new-url uri)
          conn (.openConnection url)
          reader (io/reader (.getInputStream conn))
          writer (io/writer (.getOutputStream conn))]
      (assoc this :connection conn)
      (assoc this :writer writer)
      (assoc this :reader reader)))
  (stop [this]
    (inet/close-url-connection (:connection this))))
