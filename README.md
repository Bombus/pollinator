# What is Triplidefier

This project was forked from [VertNet Gulo](https://github.com/VertNet/gulo) and subsequently butchered. The goal is to experiment with triplifing Darwin Core records for use in the semantic web.

# Expermient

The initial goal is to take a CSV file with Darwin Core records, `dwc.csv`, and MapReduce over it for two outputs. 

First output, a CSV file called `type.csv` that contains a line for each `Taxon`, `Occurrence`, and `Event` in `dwc.csv` along with a UUID. `Taxon` will come from `ScientificName`, `Occurrence` will be the original record itself, and `Event` will be a combination of `Locality`, `Date` ,and `RecordedBy`.

Second output, a CSV file called `source_of.csv` that contains three UUIDs: The UUID of an `Event` or `Taxon`, the UUID of the `Occurrence` that sources it, and a UUID for the row itself.

For example, given the following `dwc.csv` file:

```
scientificname,recordedby,date,locality
puma concolor,rob guralnick,8/8/2012,berkeley
```

The `type.csv` would look like this:

```
type_uuid,type,object
c2d2ffb3-02f2-48a7-a021-282bb8447123,occurrence,"puma concolor,rob guralnick,8/8/2012,berkeley"
05197959-5b88-414c-b97d-86dd79c5553b,taxon,puma concolor
75197959-5b88-414c-b97d-86dd79c5553b,"rob guralnick,8/8/2012,berkeley"
```

The `source_of.csv` would look like this:

```
source_uuid,type_uuid,object_uuid
cffbc118-555c-4829-bbb5-01f718f4697d,c2d2ffb3-02f2-48a7-a021-282bb8447123,05197959-5b88-414c-b97d-86dd79c5553b
65197959-5b88-414c-b97d-86dd79c5553b,c2d2ffb3-02f2-48a7-a021-282bb8447123,75197959-5b88-414c-b97d-86dd79c5553b
```
