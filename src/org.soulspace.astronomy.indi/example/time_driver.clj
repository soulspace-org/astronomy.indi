(ns org.soulspace.astronomy.indi.example.time-driver
  (:require [clojure.data.xml :as xml]
            [org.soulspace.astronomy.indi.common :as ic]
            [org.soulspace.astronomy.indi.protocol :as ip]
            [org.soulspace.astronomy.indi.driver :as id])
  (:import [org.soulspace.astronomy.indi.driver INDIDriver]))

;;
;; Driver state
;;
(def state
  (atom
    {:devices {"TimeDevice1" {"DRIVER_INFO" {:type :indi/text-vector
                                             :name "DRIVER_INFO"
                                             :device "TimeDevice1"
                                             :group "General Info"
                                             :label "Driver Info"
                                             :state "Idle"
                                             :perm :indi/ro
                                             :timeout "0"
                                             :values {"DRIVER_NAME"      {:type :indi/text
                                                                          :name "DRIVER_NAME"
                                                                          :label "Name"
                                                                          :value "TimeDriver"}
                                                      "DRIVER_EXEC"      {:type :indi/text
                                                                          :name "DRIVER_EXEC"
                                                                          :label "Exec"
                                                                          :value "indi4clj.example.time-driver"}
                                                      "DRIVER_VERSION"   {:type :indi/text
                                                                          :name "DRIVER_VERSION"
                                                                          :label "Version"
                                                                          :value "0.1"}
                                                      "DRIVER_INTERFACE" {:type :indi/text
                                                                          :name "DRIVER_INTERFACE"
                                                                          :label "Interface"
                                                                          :value "1"}}}
                              "CONNECTION" {:type :indi/switch-vector
                                            :name "CONNECTION"
                                            :device "TimeDevice1"
                                            :group "Main Control"
                                            :label "Connection"
                                            :rule "OneOfMany"
                                            :state "Idle"
                                            :perm :indi/rw
                                            :timeout "60"
                                            :values {"CONNECT"    {:type :indi/switch
                                                                   :name "CONNECT"
                                                                   :label "Connect"
                                                                   :value :indi/Off}
                                                     "DISCONNECT" {:type :indi/switch
                                                                   :name "DISCONNECT"
                                                                   :label "Disconnect"
                                                                   :value :indi/On}}}
                              "POLLING_PERIOD" {:type :indi/number-vector
                                                :name "POLLING_PERIOD"
                                                :device "TimeDevice1"
                                                :group "Options"
                                                :label "Polling"
                                                :state "Idle"
                                                :perm :indi/rw
                                                :timeout "30"
                                                :values {"PERIOD_MS" {:type :indi/number
                                                                      :name "PERIOD_MS"
                                                                      :label "Period (ms)"
                                                                      :format "%d"
                                                                      :min 10
                                                                      :max 600000
                                                                      :step 1000
                                                                      :value 10000}}}
                              "CURRENT_TIME" {:type :indi/number-vector
                                              :name "CURRENT_TIME"
                                              :device "TimeDevice1"
                                              :group "Time"
                                              :label "Current Time"
                                              :state "Idle"
                                              :perm :indi/ro
                                              :timeout "0"
                                              :values {"CURRENT_TIME_MS" {:type :indi/number
                                                                          :name "CURRENT_TIME_MS"
                                                                          :label "Current Time (ms)"
                                                                          :format "%d"
                                                                          :min 0
                                                                          :max 0
                                                                          :step 100
                                                                          :value 1596663312781}}}}}}))
;;
;; Handlers
;;
(defn send-command
  [xml]
  ; dummy implementation
  (println (xml/emit-str xml)))

(defn update-time
  "Updates the time in state and sends the update to the clients."
  [time]
  (swap! state update-in [:devices "TimeDevice1" "CURRENT_TIME" :values "CURRENT_TIME_MS"]
         assoc :value time)
  (send-command (ip/set-type-vector (ic/property @state "TimeDevice1" "CURRENT_TIME"))))

(defn get-properties
  ([]
    (let [d-names (keys (:devices @state))]
      (doseq [d-name d-names]
        (get-properties d-name))))
  ([d-name]
    (send-command (ip/del-property {:device d-name}))
    (let [device (ic/device @state d-name)
          p-names (keys device)]
    (doseq [cmd (map ipg/def-type-vector (ic/properties @state d-name))]
      (send-command cmd))))
  ([d-name p-name]
    (if-let [p (ic/property d-name p-name)]
      (send-command (ipg/def-type-vector p)))))

(defn def-property
  [p]
  (send-command (ipg/def-type-vector p)))

(defn handle-new-type-vector
  [cmd]
  (let [d-name (:device cmd)
        p-name (:name cmd)
        vs (:values cmd)]
    (when (ic/write-permission? @state d-name p-name)
      (doseq [v vs]
        (ic/update-property-value @state d-name p-name (:name v) (:value v))) ; TODO add device action, e.g. update value in device
      (send-command (ip/set-type-vector (ic/property @state d-name p-name))))))


(defmulti handle-command :type)
(defmethod handle-command :indi/getProperties
  [cmd]
  (let [d-name (:device cmd)
        p-name (:name cmd)]
    (cond
      (and d-name p-name) (get-properties d-name p-name)
      d-name (get-properties d-name)
      :else (get-properties))))
(defmethod handle-command :indi/enableBLOB)
(defmethod handle-command :indi/newBLOBVector)
;(defmethod handle-command :indi/newLightVector)
(defmethod handle-command :indi/newNumberVector)
(defmethod handle-command :indi/newSwitchVector)
(defmethod handle-command :indi/newTextVector)


(def in-chan )
(def out-chan )

; TODO: initialize channels
(defn init
  [in out]
  
  )


(comment
  ; deftype?
(defrecord TimeDriver [in-chan out-chan]
  INDIDriver
  (send-command [this cmd] ) ; not specific of a concrete driver
  (handle-command [this cmd] ) ; not specific of a concrete driver
  (handle-get-properties [this cmd] ) ; not specific of a concrete driver
  (handle-new-type-vector [this cmd]) ; specific for a driver
  ))
