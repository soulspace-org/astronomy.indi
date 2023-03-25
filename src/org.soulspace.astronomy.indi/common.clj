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

(ns org.soulspace.astronomy.indi.common)

;;;
;;; common INDI functions
;;;

;;
;; State accessors
;;

(defn device-names
  "Returns the names of all devices."
  [state]
  (keys (get-in state [:devices])))

(defn devices
  "Returns the data of all devices."
  [state]
  (vals (get-in state [:devices])))

(defn device
  "Returns the device with the given name."
  [state d-name]
  (get-in state [:devices d-name]))

(defn property-names
  "Returns the names of all properties for a given device."
  ([device]
    (keys device))
  ([state d-name]
    (property-names (get-in state [:devices d-name]))))

(defn properties
  "Returns all properties for a given device."
  ([device]
    (vals device))
  ([state d-name]
    (properties (get-in state [:devices d-name]))))

(defn property-value-names
  "Returns the names of all property values for a given property."  
  ([property]
    (keys (:values property)))
  ([state d-name p-name]
    (property-value-names (get-in state [:devices d-name p-name]))))

(defn property-values
  "Returns all property values for a given property."  
  ([property]
    (vals (:values property)))
  ([state d-name p-name]
    (property-value-names (get-in state [:devices d-name p-name]))))

(defn property
  "Returns a property by path or by device and property name."
  ([state path]
    (get-in state path))
  ([state d-name p-name]
    (property state [:devices d-name p-name])))

(defn value
  "Returns a value by path or by device property name and value name."
  ([state path]
    (get-in state path))
  ([state d-name p-name v-name]
    (value state [:devices d-name p-name :values v-name])))

(defn write-permission?
  "Checks for write permissions on the property."
  ([property]
   (= (:perm property) :indi/rw))
  ([state d-name p-name]
   (write-permission? (get-in state [:devices d-name p-name]))))

(defn update-property-value
  "Updates the property value."
  ([state d-name p-name pv-name v]
    (swap! state update-in [:devices d-name p-name :values pv-name] assoc :value v)))

(defn update-property-state
  "Updates the property state."
  ([state d-name p-name v]
    (swap! state update-in [:devices d-name p-name] assoc :state v)))
