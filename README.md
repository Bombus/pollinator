# What is Pollinator

This project was forked from [VertNet Gulo](https://github.com/VertNet/gulo) and subsequently butchered. The goal is to experiment with constructing graph-based views of Darwin Core objects in MapReduce, enabling integration with related objects on the semantic web.

# Experiment

Goal 1: Take a CSV file with Darwin Core records, `dwc.csv`, and MapReduce over it to create the following outputs:

First output, a CSV file called `type.csv` that contains a line for each `Taxon`, `Occurrence`, and `Event` in `dwc.csv` along with a UUID. `Taxon` will come from `ScientificName`, `Occurrence` will be the original record itself, and `Event` will be a combination of `Locality`, `Date` ,and `RecordedBy`.  Utilize both a UUID identifier and a QUID identifier -- the purpose of the QUID is to experiment with property-based identifiers.

Second output, a CSV file called `source_of.csv` that contains three UUIDs: A UUID for the row itself (primary key), the UUID of a source object, and a UUID of a target object (The source is the "source_of" the target).

For example, given the following `dwc.csv` file:

```
scientificname,recordedby,date,locality
puma concolor,rob guralnick,8/8/2012,berkeley
```

The `type.csv` would look like this:

```
object_uuid,object_quid,type 
urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123,urn:x-quid:t=puma+concolor;c=rob+guralnick;d=8/8/2012;l=berkeley,dwc:occurrence
urn:uuid:5197959-5b88-414c-b97d-86dd79c5553b,urn:x-quid:t=puma+concolor,dwc:taxon
urn:uuid:75197959-5b88-414c-b97d-86dd79c5553b,urn:x-quid:c=rob+guralnick;d=8/8/2012;l=berkeley,dwc:event
```

The `source_of.csv` would look like this:

```
pk_uuid,source_uuid,target_uuid
urn:uuid:cffbc118-555c-4829-bbb5-01f718f4697d,urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123,urn:uuid:05197959-5b88-414c-b97d-86dd79c5553b
urn:uuid:65197959-5b88-414c-b97d-86dd79c5553b,urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123,urn:uuid:75197959-5b88-414c-b97d-86dd79c5553b
```

Goal 2: 
Read the above outputs into a MapReduce framework.

Goal 3: 
Write a query in Java against the data in the MapReduce framework for a single UUID as a query parameter that returns an N3 file containing "source_of" relationships and types.  For example:

```
function String query(urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123) {
  ...
  return String;
}
```


Would return:
```
  urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123 a dwc:occurrence .
  urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123 ma:source_of urn:uuid:05197959-5b88-414c-b97d-86dd79c5553b .
  urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123 ma:source_of urn:uuid:75197959-5b88-414c-b97d-86dd79c5553b .
```
