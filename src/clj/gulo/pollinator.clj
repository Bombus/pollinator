(ns gulo.pollinator
  "This namespace provides support for the pollinator experiment."
  (:use [gulo.core :as core]
        [gulo.util :as util :only (latlon-valid? gen-uuid)]
        [cascalog.api]
        [cascalog.more-taps :as taps :only (hfs-delimited)]
        [dwca.core :as dwca])
  (:require [clojure.string :as s]
            [clojure.java.io :as io])
  (:import [edu.ucsb.nceas.ezid EZIDService]))

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

(defn loc
  "Return 3-tuple [occid lat lon] from supplied textline."
  [line]
  (let [vals (split-line line)]
    (map (partial nth vals) [4 5])))

(defn tax
  "Return 7-tuple [kingdom phylum class order family genus scientificname] from
  supplied textline."
  [line]
  (let [vals (split-line line)]
    (map (partial nth vals) [2])))

(defn locname
  "Return 4-tuple [occid lat lon name] from supplied textline."
  [line]
  (let [[lat lon] (loc line)
        [s] (tax line)]
    [lat lon s]))

(defn occ-source-query
  "Execute query to create occurrence source-of taxon table."
  [occ-path tax-path loc-path sink-path]
  (let [occ-source (hfs-textline occ-path)
        tax-source (hfs-textline tax-path)
        loc-source (hfs-textline loc-path)
        sink (hfs-textline sink-path :sinkmode :replace)]
    (?<- sink
         [?line]
         (tax-source ?tax-line)
         (split-line ?tax-line :> ?tax-id ?tax-type ?scientificname)
         (occ-source ?occ-line)
         (split-line ?occ-line :> ?ezid ?occ-type ?catalognumber ?recordedby
                     ?scientificname ?eventdate ?decimallatitude ?decimallongitude
                     ?identifiedby)
         (core/makeline ?ezid ?tax-id :> ?line)
         (:distinct true))))

(defn loc-source-query
  "Execute query to create location source-of occurrence table."
  [occ-path tax-path loc-path sink-path]
  (let [occ-source (hfs-textline occ-path)
        tax-source (hfs-textline tax-path)
        loc-source (hfs-textline loc-path)
        sink (hfs-textline sink-path :sinkmode :replace)]
    (?<- sink
         [?line]
         (loc-source ?loc-line)
         (split-line ?loc-line :> ?loc-id ?loc-type ?decimallatitude ?decimallongitude)
         (occ-source ?occ-line)
         (split-line ?occ-line :> ?ezid ?occ-type ?catalognumber ?recordedby
                     ?scientificname ?eventdate ?decimallatitude ?decimallongitude
                     ?identifiedby)
         (core/makeline ?loc-id ?ezid :> ?line)
         (:distinct true))))

(defn occ-query
  "Create type.csv output for occurrences by generating uniques with EZIDs."
  [source sink-path]
  (let [sink (hfs-textline sink-path :sinkmode :replace)]
    (?<- sink
         [?line]
         (source ?occ)
         (util/mint-ezid :> ?ezid)
         (core/makeline ?ezid "dwc:Occurrence" ?occ :> ?line))))

(defn tax-query
  "Create type.csv output for taxonomies by generating uniques with EZIDs."
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
         (util/mint-ezid :> ?ezid)
         (core/makeline ?ezid "dwc:Taxon" ?s :> ?line))))

(defn loc-query
  "Create type.csv output for locations by generating uniques with EZIDs."
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
         (util/mint-ezid :> ?ezid)
         (core/makeline ?ezid "dwc:Locality" ?lat ?lon :> ?line)
         (:distinct true))))

(defn triplify!
  "Triplify supplied input data source and save outputs to hfs directory."
  [hfs data]
  (let [source (hfs-textline data)
        loc-sink (str hfs "/loc")
        tax-sink (str hfs "/tax")
        occ-sink (str hfs "/occ")
        locsource-sink (str hfs "/locsource")
        occsource-sink (str hfs "/occsource")]
    (loc-query source loc-sink)
    (tax-query source tax-sink)
    (occ-query source occ-sink)
    (loc-source-query occ-sink tax-sink loc-sink locsource-sink)
    (occ-source-query occ-sink tax-sink loc-sink occsource-sink)))
