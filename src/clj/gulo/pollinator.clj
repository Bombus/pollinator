(ns gulo.pollinator
  "This namespace provides support for the pollinator experiment."
  (:use [gulo.core :as core]
        [gulo.util :as util :only (latlon-valid? gen-uuid)]
        [cascalog.api]
        [cascalog.more-taps :as taps :only (hfs-delimited)]
        [dwca.core :as dwca])
  (:require [clojure.string :as s]
            [clojure.java.io :as io])
  (:import [org.gbif.dwc.record DarwinCoreRecord]))

;; Ordered column names from the input occurrence file.
(def occ-fields ["?occurrenceid" "?catalognumber" "?recordedby" "?scientificname"
                 "?eventdate" "?decimallatitude" "?decimalongitude" "?identifiedby"])

;; Ordered column names from the input occurrence file.
(def out-fields ["?occurrenceid" "?tax-id" "?loc-id" "?catalognumber"
                 "?recordedby" "?scientificname" "?eventdate"
                 "?decimallatitude" "?decimallongitude" "?identifiedby"])

;; Position of values in a texline.
(def PERSON 2)
(def SNAME 3)
(def DATE 4)
(def LATI 5)
(def LONI 6)

(defn split-line
  "Returns vector of line values by splitting on tab."
  [line]
  (vec (.split line "\t")))

;; (defn id
;;   "Return occid from supplied textline."
;;   [line]
;;   (nth (split-line line) OCCID))

(defn loc
  "Return 3-tuple [occid lat lon] from supplied textline."
  [line]
  (let [vals (split-line line)]
    (map (partial nth vals) [LATI LONI])))

(defn tax
  "Return 7-tuple [kingdom phylum class order family genus scientificname] from
  supplied textline."
  [line]
  (let [vals (split-line line)]
    (map (partial nth vals) [SNAME])))

(defn locname
  "Return 4-tuple [occid lat lon name] from supplied textline."
  [line]
  (let [[lat lon] (loc line)
        [s] (tax line)]
    [lat lon s]))

(defn output-query
  "Execute query against supplied source of occ, loc, tax, and taxloc textlines
  for occurrence ouput rows that include GUIDs for taxonomy and location. Sinks
  rows to supplied sink path."
  [occ-path tax-path loc-path sink-path]
  (let [result-vector out-fields
        occ-source (hfs-textline occ-path)
        tax-source (hfs-textline tax-path)
        loc-source (hfs-textline loc-path)
        sink (taps/hfs-delimited sink-path :sinkmode :replace)        
        uniques (<- [?tax-id ?loc-id ?s ?lat ?lon]
                    (tax-source ?tax-line)
                    (split-line ?tax-line :> ?tax-id ?s)
                    (loc-source ?loc-line)
                    (split-line ?loc-line :> ?loc-id ?lat ?lon)
                    (occ-source ?occ-line)
                    (locname ?occ-line :> ?lat ?lon ?s))]
    (?<- sink
         out-fields
         (uniques ?tax-id ?loc-id ?scientificname ?decimallatitude ?decimallongitude)
         (occ-source ?occ-line)
         (split-line ?occ-line :>> occ-fields))))

(defn occ-query
  "Execute query against supplied source of occurrence textlines that adds UUIDs
  to each record."
  [source sink-path]
  (let [sink (hfs-textline sink-path :sinkmode :replace)]
    (?<- sink
         [?line]
         (source ?occ)
         (util/gen-uuid :> ?uuid)
         (core/makeline ?uuid ?occ :> ?line))))

(defn tax-query
  "Execute query against supplied source of occurrence textlines for unique
  taxonomies. Sink tuples [uuid kingdom phylum class order family genus scientificname]
  to sink-path."
  [source sink-path]
  (let [sink (hfs-textline sink-path :sinkmode :replace)
        uniques (<- [?s]
                     (source ?line)
                     (tax ?line :> ?s)
                     (core/valid-name? ?s)
                     (:distinct true))]
    (?<- sink
         [?line]
         (uniques ?s)
         (util/gen-uuid :> ?uuid)
         (core/makeline ?uuid ?s :> ?line))))

(defn loc-query
  "Execute query against supplied source of occurrence textlines for unique
  coordinates. Sink tuples [uuid lat lon wkt] to sink-path."
  [source sink-path]
  (let [sink (hfs-textline sink-path :sinkmode :replace)
        uniques (<- [?lat ?lon]
                    (source ?line)
                    (loc ?line :> ?lat ?lon)
                    (util/latlon-valid? ?lat ?lon)
                    (:distinct true))]
    (?<- sink
         [?line]
         (uniques ?lat ?lon)
         (util/gen-uuid :> ?uuid)
         (core/makeline ?uuid ?lat ?lon :> ?line)
         (:distinct true))))
