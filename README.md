# What is Pollinator

This project was forked from [VertNet Gulo](https://github.com/VertNet/gulo) and subsequently butchered. The goal is to experiment with constructing graph-based views of Darwin Core objects in MapReduce, enabling integration with related objects on the semantic web.

# Experiment

## Goal 1

Take a CSV file with Darwin Core records, `dwc.csv`:

```
catalogNumber,recordedBy,scientificName,eventDate,decimalLatitude,decimalLongitude,identifiedBy
1,Rob Guralnick,Puma concolor,8/8/12,37.1,-120.1,Aaron Steele
2,John Deck,Ursus arctos horribilis,8/8/12,37.2,-120.1,Rob Guralnick
3,Aaron Steele,Bufu bufo,8/8/12,37.2,-120.2,Dave Wake
4,Neil Davies,Aedes washinoi,8/8/12,37.1,-120.2,Neil Davies
6,Michelle Koo,Zaedyus pichiy,8/8/12,37.1,-120.3,Michelle Koo
7,John Kunze,Carduelis tristis,8/8/12,37.3,-120.3,John Kunze
8,Nico Cellinese,Acanthella pulchra,8/8/12,37.3,-120.4,Nico Cellinese
9,Sarah Hinman,Culex Tarsalis,8/8/12,37.3,-120.5,Sarah Hinman
```

Then we'll then MapReduce over it to create some outputs.

### First output

A CSV file called `type.csv` that contains a line for each `Taxon`, `Occurrence`, and `Event` in `dwc.csv` along with a UUID. `Taxon` will come from `ScientificName`, `Occurrence` will be the original record itself, and `Event` will be a combination of `Locality`, `Date` ,and `RecordedBy`.  Utilize both a UUID identifier and a QUID identifier -- the purpose of the QUID is to experiment with property-based identifiers.

### Second output

CSV file called `related_to.csv` that contains three UUIDs: 

1. A UUID for the row itself (primary key)
2. UUID of an object
3. UUID of a another object

These objects contain a non-directed relation, and in fact, represent different identifiers for the same thing.  

### Third output

A CSV file called `source_of.csv` that contains three UUIDs: 

1. A UUID for the row itself (primary key)
2. UUID of a source object
3. UUID of a target object (The source is the `source_of` the target)

For example, given the following `dwc.csv` file:

```
scientificname,recordedby,date,locality
puma concolor,rob guralnick,8/8/2012,berkeley
```

The `type.csv` would look like this:

```
object_id,type 
urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123,dwc:occurrence
urn:uuid:5197959-5b88-414c-b97d-86dd79c5553b,dwc:taxon
urn:uuid:75197959-5b88-414c-b97d-86dd79c5553b,dwc:event
urn:x-quid:t=puma+concolor;c=rob+guralnick;d=8/8/2012;l=berkeley,dwc:occurrence
urn:x-quid:t=puma+concolor,dwc:taxon
urn:x-quid:c=rob+guralnick;d=8/8/2012;l=berkeley,dwc:event

```

The `related_to.csv` would look like this:

```
pk_id,object1_id,object2_id 
urn:uuid:cffbc118-555c-4829-bbb5-01f718f4697e,urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123,urn:x-quid:t=puma+concolor;c=rob+guralnick;d=8/8/2012;l=berkeley
urn:uuid:cffbc118-555c-4829-bbb5-01f718f4697f,urn:uuid:5197959-5b88-414c-b97d-86dd79c5553b,urn:x-quid:t=puma+concolor
urn:uuid:cffbc118-555c-4829-bbb5-01f718f4697g,urn:uuid:75197959-5b88-414c-b97d-86dd79c5553b,urn:x-quid:c=rob+guralnick;d=8/8/2012;l=berkeley
```

The `source_of.csv` would look like this:

```
pk_id,source_id,target_id
urn:uuid:cffbc118-555c-4829-bbb5-01f718f4697d,urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123,urn:uuid:05197959-5b88-414c-b97d-86dd79c5553b
urn:uuid:65197959-5b88-414c-b97d-86dd79c5553b,urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123,urn:uuid:75197959-5b88-414c-b97d-86dd79c5553b
```

Do the same on a test dataset

## Goal 2

Bulk to CSV files to AppEngine datastore.

## Goal 3

Write a query API and client, then construct a query against that in Java against the data in the AppEngine datastore for a single UUID as a query parameter that returns an N3 file containing `source_of` relationships and types.  For example:

```java
public interface Query {
  public static String execute(String uuid);
}
```


Would return:
```
  urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123 a dwc:occurrence .
  urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123 ma:source_of urn:uuid:05197959-5b88-414c-b97d-86dd79c5553b .
  urn:uuid:c2d2ffb3-02f2-48a7-a021-282bb8447123 ma:source_of urn:uuid:75197959-5b88-414c-b97d-86dd79c5553b .
```
## Goal 4

Sketch out EZID use cases, implementation from context of the above exercises.  Best way to assign identifiers in the above process.

Outputs:
Subversion code used is from  https://code.ecoinformatics.org/code/ezid/trunk/ as a maven project.  We mint identifiers like:
```java
EZIDService ezid = new EZIDService();
ezid.login(username,password);
// ark:/99999/fk4 is the temporary ark shoulder
String newId = ezid.mintIdentifier("ark:/99999/fk4", null);
```
