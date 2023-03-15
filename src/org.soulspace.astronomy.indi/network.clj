(ns org.soulspace.astronomy.indi.network
  (:import [java.net InetSocketAddress Socket URL URLConnection URLStreamHandler URLStreamHandlerFactory]))

(def indi-protocol-schema "indi")
(def indi-default-port 7624)
(def default-timeout 30000)

(def indi-socket (atom nil))

(defn close-url-connection
  [url-connection]
  (when @indi-socket
    (.close @indi-socket)
    (reset! indi-socket nil))
  (.close (.getInputStream url-connection))
  (.close (.getOutputStream url-connection)))

;;
;; Java interop to handle the INDI URL protocol (indi://<host>:<port>)
;;

(defn new-url-connection
  "Creates an URLConnection."
  [url]
  (let [host (.getHost url) ; TODO set default on missing value
        port (.getPort url) ; TODO set default on missing value
        addr (InetSocketAddress. host port)
        socket (Socket.)
        url-connection (proxy [URLConnection] [url]
      (connect []
        (println "Connecting to url...")
        (.connect socket addr default-timeout))
      (getInputStream []
        (.getInputStream socket))
      (getOutputStream []
        (.getOutputStream socket)))]
    (.setDoInput url-connection true)
    (.setDoOutput url-connection true)
    url-connection))

(defn new-url
  [uri]
  (URL. uri))

(defn new-indi-url-stream-handler
  "Creates an URLStreamHandler for the INDI protocol."
  []
  (proxy [URLStreamHandler] []
    (openConnection [url]
      (let [url-connection (new-url-connection url)]
        (.connect url-connection)
        url-connection))
    (getDefaultPort [] indi-default-port)))

(defn new-url-stream-handler-factory
  "Creates an URLStreamHandlerFactory which creates INDI URLStreamHandlers."
  []
  (reify URLStreamHandlerFactory
    (createURLStreamHandler ^URLStreamHandler [this protocol]
      (cond
        (= protocol indi-protocol-schema) (new-indi-url-stream-handler)))))

(defn register-indi-protocol
  "Registers the URLStreamHandlerFactory for the INDI protocol."
  []
  (URL/setURLStreamHandlerFactory (new-url-stream-handler-factory))
  true)

; register the INDI protocol
(defonce indi-protocol-registered (register-indi-protocol))

