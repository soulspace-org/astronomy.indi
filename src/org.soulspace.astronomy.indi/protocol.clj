(ns org.soulspace.astronomy.indi.protocol
  (:require [clojure.string :as str]
            [clojure.data.xml :as xml]
            [clojure.spec.alpha :as s]
            [org.soulspace.clj.namespace :as nsp]
            [org.soulspace.clj.java.codec :as codec]
            [org.soulspace.xml.dsl.builder :as dsl]))

(def protocol-version "1.7")

(def indi-tags ["defBLOB" "defBLOBVector" "defLight" "defLightVector"
                "defNumber" "defNumberVector" "defSwitch" "defSwitchVector"
                "defText" "defTextVector" "delProperty" "enableBLOB"
                "getProperties" "message"
                "newBLOBVector" "newLightVector" "newNumberVector" "newSwitchVector" "newTextVector"
                "oneBLOB" "oneLight" "oneNumber" "oneSwitch" "oneText"
                "setBLOBVector" "setLightVector" "setNumberVector" "setSwitchVector" "setTextVector"])

;;
;; Attribute value sets
;;

(def property-state #{:indi/Idle :indi/Ok :indi/Busy :indi/Alert})
(def switch-state #{:indi/Off :indi/On})
(def switch-rule #{:indi/OneOfMany :indi/AtMostMany :indi/AnyOfMany})
(def property-perm #{:indi/ro :indi/wo :indi/rw})
(def blob-enable #{:indi/Never :indi/Also :indi/Only})

;;
;; spec for the INDI protocol 
;;

;; attribute specification
(s/def :indi/type keyword?)
(s/def :indi/blob-enable blob-enable)
(s/def :indi/device (s/nilable string?))
(s/def :indi/format (s/nilable string?))
(s/def :indi/group (s/nilable string?))
(s/def :indi/name (s/nilable string?))
(s/def :indi/min (s/nilable int?))
(s/def :indi/max (s/nilable int?))
(s/def :indi/step (s/nilable int?))
(s/def :indi/message (s/nilable string?))
(s/def :indi/name (s/nilable string?))
(s/def :indi/perm property-perm)
(s/def :indi/rule switch-rule)
(s/def :indi/state property-state)
(s/def :indi/switch-state switch-state)
(s/def :indi/timestamp inst?)
(s/def :indi/timeout (s/nilable int?))

;; element specification
;;(s/def :indi/getProperties)
(s/def :indi/delProperty (s/keys :req [:indi/device]))
(s/def :indi/enableBLOB (s/keys :req [:indi/device]))
(s/def :indi/oneBLOB (s/keys :req [:indi/name :indi/size :indi/format]))
(s/def :indi/defBLOB (s/keys :req [:indi/name]))
(s/def :indi/defBLOBVector (s/keys :req [:indi/device :indi/name :indi/perm :indi/state]))
(s/def :indi/newBLOBVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/setBLOBVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/oneLight (s/keys :req [:indi/name]))
(s/def :indi/defLight (s/keys :req [:indi/name]))
(s/def :indi/defLightVector (s/keys :req [:indi/device :indi/name :indi/state]))
;;(s/def :indi/newLightVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/setLightVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/oneNumber (s/keys :req [:indi/name]))
(s/def :indi/defNumber (s/keys :req [:indi/name :indi/format :indi/max :indi/min :indi/step]))
(s/def :indi/defNumberVector (s/keys :req [:indi/device :indi/name :indi/perm :indi/state]))
(s/def :indi/newNumberVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/setNumberVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/oneSwitch (s/keys :req [:indi/name]))
(s/def :indi/defSwitch (s/keys :req [:indi/name]))
(s/def :indi/defSwitchVector (s/keys :req [:indi/device :indi/name :indi/perm :indi/state :indi/rule]))
(s/def :indi/newSwitchVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/setSwitchVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/oneText (s/keys :req [:indi/name]))
(s/def :indi/defText (s/keys :req [:indi/name]))
(s/def :indi/defTextVector (s/keys :req [:indi/device :indi/name :indi/perm :indi/state]))
(s/def :indi/newTextVector (s/keys :req [:indi/device :indi/name]))
(s/def :indi/setTextVector (s/keys :req [:indi/device :indi/name]))


;;
;; INDI Protocol Command Parsing
;;

(defn remove-empty-keys
  "Returns a map with all keys, for which the value is nil, are removed."
  [m]
  (into {} (remove (comp nil? val) m)))

;;
;; Parse INDI XML Attributes
;;

;; formats seen in the wild: %.f %g 
;; HMS/DMS (%<w>.<f>m)
(defn parse-number-format
  [f]
  (str/trim (str f)))

(defn parse-number-value
  "Returns the parsed number value."
  ([n]
   (str/trim (str/join n)))
  ([format n]
   (str/trim (str/join n))))

(defn parse-time-value
  "Returns the parsed time value."
  [t]
  (when (seq t)
    (clojure.instant/read-instant-timestamp t)))

(defn parse-property-state
  "Returns the parsed switch value as keyword."
  [s]
  (keyword "indi" (str/trim (str/join s))))

(defn parse-switch-state
  "Returns the parsed switch value as keyword."
  [s]
  (keyword "indi" (str/trim (str/join s))))

(defn parse-text-value
  "Returns the trimmed text value."
  [s]
  (str/trim (str/join s)))

(defn parse-permission
  "Returns the parsed permission as keyword."
  [s]
  (keyword "indi" (str/trim (str/join s))))

(defn parse-state
  "Returns the parsed state as keyword."
  [s]
  (keyword "indi" (str/trim (str/join s))))

;;
;; Parse INDI XML Tags
;;

(defn parse-getProperties
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/getProperties
   :indi/version (get-in element [:attrs :version])
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])})

(defn parse-delProperty
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/delProperty
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])})

(defn parse-defBLOB
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defBLOB
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])})

(defn parse-oneBLOB
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/oneBLOB
   :indi/name (get-in element [:attrs :name])
   :indi/size (parse-number-value (get-in element [:attrs :size]))
   :indi/format (get-in element [:attrs :format])
   :indi/value (:content element)})

(defn parse-defBLOBVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defBLOBVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/group (get-in element [:attrs :group])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/perm (parse-permission (get-in element [:attrs :perm]))
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-defBLOB (:content element))})

(defn parse-newBLOBVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/newBLOBVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/values (map parse-oneBLOB (:content element))})

(defn parse-setBLOBVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/setBLOBVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-oneBLOB (:content element))})

(defn parse-defLight
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defLight
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/value (:content element)})

(defn parse-oneLight
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/oneLight
   :indi/name (get-in element [:attrs :name])
   :indi/value (:content element)})

(defn parse-defLightVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defLightVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/group (get-in element [:attrs :group])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-defLight (:content element))})

(defn parse-newLightVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/newLightVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/values (map parse-oneLight (:content element))})

(defn parse-setLightVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/setLightVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/state (parse-state (get-in element [:attrs :state]))
;   :timeout ""
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-oneLight (:content element))})

(defn parse-defNumber
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defNumber
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/format (parse-number-format (get-in element [:attrs :format]))
   :indi/min (parse-number-value (get-in element [:attrs :min]))
   :indi/max (parse-number-value (get-in element [:attrs :max]))
   :indi/step (parse-number-value (get-in element [:attrs :step]))
   :indi/value (parse-number-value (:content element))})

(defn parse-oneNumber
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/oneNumber
   :indi/name (get-in element [:attrs :name])
   :indi/value (parse-number-value (:content element))})

(defn parse-defNumberVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defNumberVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/group (get-in element [:attrs :group])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/perm (parse-permission (get-in element [:attrs :perm]))
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-defNumber (:content element))})

(defn parse-newNumberVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/newNumberVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/values (map parse-oneNumber (:content element))})

(defn parse-setNumberVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/setNumberVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-oneNumber (:content element))})

(defn parse-defSwitch
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defSwitch
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/value (parse-switch-state (:content element))})

(defn parse-oneSwitch
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/oneSwitch
   :indi/name (get-in element [:attrs :name])
   :indi/value (parse-switch-state (:content element))})

(defn parse-defSwitchVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defSwitchVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/group (get-in element [:attrs :group])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/perm (parse-permission (get-in element [:attrs :perm]))
   :indi/rule (get-in element [:attrs :rule])
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-defSwitch (:content element))})

(defn parse-newSwitchVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/newSwitchVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/values (map parse-oneSwitch (:content element))})

(defn parse-setSwitchVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/setSwitchVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-oneSwitch (:content element))})

(defn parse-defText
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defText
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/value (parse-text-value (:content element))})

(defn parse-oneText
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/oneText
   :indi/name (get-in element [:attrs :name])
   :indi/value (parse-text-value (:content element))})

(defn parse-defTextVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/defTextVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/label (get-in element [:attrs :label])
   :indi/group (get-in element [:attrs :group])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/perm (parse-permission (get-in element [:attrs :perm]))
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-defText (:content element))})

(defn parse-newTextVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/newTextVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/values (map parse-oneText (:content element))})

(defn parse-setTextVector
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/setTextVector
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/state (parse-state (get-in element [:attrs :state]))
   :indi/timeout (parse-number-value (get-in element [:attrs :timeout]))
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])
   :indi/values (map parse-oneText (:content element))})

(defn parse-enableBLOB
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/enableBLOB
   :indi/device (get-in element [:attrs :device])
   :indi/name (get-in element [:attrs :name])
   :indi/value (:content element)})

(defn parse-message
  "Returns the parsed map for the element."
  [element]
  {:indi/type :indi/message
   :indi/device (get-in element [:attrs :device])
   :indi/timestamp (parse-time-value (get-in element [:attrs :timestamp]))
   :indi/message (get-in element [:attrs :message])})

(defn parse-indi
  "Returns the list of parsed maps for the element."
  [element]
  ;;(println "parse-indi" element)
  (let [tag (:tag element)]
    (cond
      (= tag :message) (parse-message element)
      (= tag :enableBLOB) (parse-enableBLOB element)
      (= tag :delProperty) (parse-delProperty element)
      (= tag :getProperties) (parse-getProperties element)
      (= tag :defBLOBVector) (parse-defBLOBVector element)
      (= tag :defLightVector) (parse-defLightVector element)
      (= tag :defNumberVector) (parse-defNumberVector element)
      (= tag :defSwitchVector) (parse-defSwitchVector element)
      (= tag :defTextVector) (parse-defTextVector element)
      (= tag :newBLOBVector) (parse-newBLOBVector element)
      (= tag :newLightVector) (parse-newLightVector element)
      (= tag :newNumberVector) (parse-newNumberVector element)
      (= tag :newSwitchVector) (parse-newSwitchVector element)
      (= tag :newTextVector) (parse-newTextVector element)
      (= tag :setBLOBVector) (parse-setBLOBVector element)
      (= tag :setLightVector) (parse-setLightVector element)
      (= tag :setNumberVector) (parse-setNumberVector element)
      (= tag :setSwitchVector) (parse-setSwitchVector element)
      (= tag :setTextVector) (parse-setTextVector element))))

(defn parse-xml
  [input]
  ;; (println "parse-xml" input)
  (->> (xml/parse input)
       (:content)
       (map parse-indi)))

(comment
  (let [responses (slurp "resources/indi-server-response.xml")]
    ;; (println responses)
    (->> (str "<indi>" responses "</indi>")
         (xml/parse-str)
         (:content)
         (map parse-indi))))

;;
;; INDI Protocol Command Generation
;;

;;
;; INDI XML DSL definition
;;

;; define functions for every XML tag 
(dsl/deftags ["defBLOB" "defBLOBVector" "defLight" "defLightVector"
              "defNumber" "defNumberVector" "defSwitch" "defSwitchVector"
              "defText" "defTextVector" "delProperty" "enableBLOB"
              "getProperties" "message"
              "newBLOBVector" "newLightVector" "newNumberVector" "newSwitchVector" "newTextVector"
              "oneBLOB" "oneLight" "oneNumber" "oneSwitch" "oneText"
              "setBLOBVector" "setLightVector" "setNumberVector" "setSwitchVector" "setTextVector"])

;;
;; Helper functions
;;

(defn format-number
  "Formats the 'number' according to the C printf format 'fmt'"
  [fmt number]
  (cond
    (= "%.f" fmt) (format "%f" (double number))
    (= "%d" fmt) (format "%d" (long number))
    (= "%g" fmt) (format "%g" (double number))
    :else (str number)))

;;
;; Property methods
;;

;; one element
(defmulti one-type :type)

(defmethod one-type :indi/blob
  [v]
  (one-blob {:name (:name v) :size (:size v) :format (:format v)}
            (codec/bytes->base64 (:value v))))

(defmethod one-type :indi/light
  [v]
  (one-light {:name (:name v) :value (name (:value v))}))

(defmethod one-type :indi/number
  [v]
  (one-number {:name (:name v) :value (format-number (:format v) (:value v))}))

(defmethod one-type :indi/switch
  [v]
  (one-switch {:name (:name v) :value (name (:value v))}))

(defmethod one-type :indi/text
  [v]
  (one-text {:name (:name v) :value (:value v)}))

;; def element
(defmulti def-type :type)

(defmethod def-type :indi/blob
  [v]
  (def-blob {:name (:name v) :size (:size v) :format (:format v)}
    (codec/bytes->base64 (:value v))))

(defmethod def-type :indi/light
  [v]
  (def-light {:name (:name v) :value (name (:value v))}))

(defmethod def-type :indi/number
  [v]
  (def-number {:name (:name v) :value (format-number (:format v) (:value v))}))

(defmethod def-type :indi/switch
  [v]
  (def-switch {:name (:name v) :value (name (:value v))}))

(defmethod def-type :indi/text
  [v]
  (def-text {:name (:name v) :value (:value v)}))

;; def vector
(defmulti def-type-vector :type)

(defmethod def-type-vector :indi/blob-vector
  [v]
  (def-blob-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                    :timestamp (java.time.Instant/now)}
    (map def-type (vals (:values v)))))

(defmethod def-type-vector :indi/light-vector
  [v]
  (def-light-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                     :timestamp (java.time.Instant/now)}
    (map def-type (vals (:values v)))))

(defmethod def-type-vector :indi/number-vector
  [v]
  (def-number-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                      :timestamp (java.time.Instant/now)}
    (map def-type (vals (:values v)))))

(defmethod def-type-vector :indi/switch-vector
  [v]
  (def-switch-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                      :timestamp (java.time.Instant/now)}
    (map def-type (vals (:values v)))))

(defmethod def-type-vector :indi/text-vector
  [v]
  (def-text-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                    :timestamp (java.time.Instant/now)}
    (map def-type (vals (:values v)))))

;; set vector
(defmulti set-type-vector :type)

(defmethod set-type-vector :indi/blob-vector
  [v]
  (let [vs (vals (:values v))]
    (set-blob-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                      :timestamp (java.time.Instant/now)}
                     (map one-type vs))))

(defmethod set-type-vector :indi/light-vector
  [v]
  (set-light-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                     :timestamp (java.time.Instant/now)}
                    (map one-type (vals (:values v)))))

(defmethod set-type-vector :indi/number-vector
  [v]
  (set-number-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                      :timestamp (java.time.Instant/now)}
                     (map one-type (vals (:values v)))))

(defmethod set-type-vector :indi/switch-vector
  [v]
  (set-switch-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                      :timestamp (java.time.Instant/now)}
                     (map one-type (vals (:values v)))))

(defmethod set-type-vector :indi/text-vector
  [v]
  (set-text-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                    :timestamp (java.time.Instant/now)}
                   (map one-type (vals (:values v)))))

;; new vector
(defmulti new-type-vector :type)

(defmethod new-type-vector :indi/blob-vector
  [v]
  (new-blob-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                    :timestamp (java.time.Instant/now)}
                   (map one-type (vals (:values v)))))

(defmethod new-type-vector :indi/light-vector
  [v]
  (new-light-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                     :timestamp (java.time.Instant/now)}
                    (map one-type (vals (:values v)))))

(defmethod new-type-vector :indi/number-vector
  [v]
  (new-number-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                      :timestamp (java.time.Instant/now)}
                     (map one-type (vals (:values v)))))

(defmethod new-type-vector :indi/switch-vector
  [v]
  (new-switch-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                      :timestamp (java.time.Instant/now)}
                     (map one-type (vals (:values v)))))

(defmethod new-type-vector :indi/text-vector
  [v]
  (new-text-vector {:name (:name v) :device (:device v) :state (name :indi/Ok)
                    :timestamp (java.time.Instant/now)}
                   (map one-type (vals (:values v)))))

