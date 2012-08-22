# What is Pollinator

This project was forked from [VertNet Gulo](https://github.com/VertNet/gulo) and subsequently butchered. The goal is to experiment with constructing graph-based views of Darwin Core objects in MapReduce, enabling integration with related objects on the semantic web.

# Experiment

See the [Runtime](https://github.com/Bombus/pollinator/wiki/Runtime) wiki page for how to run the follwoing Pollinator experiments on your laptop.

## Goal 1

Take a CSV file with Darwin Core records, `dwc.csv`:

```
catalogNumber,recordedBy,scientificName,eventDate,decimalLatitude,decimalLongitude,identifiedBy
1,Rob Guralnick,Puma concolor,8/8/12,37.1,-120.1,Aaron Steele
2,John Deck,Ursus arctos horribilis,8/8/12,37.2,-120.1,Rob Guralnick
3,Aaron Steele,Bufu bufo,8/8/12,37.2,-120.2,Dave Wake
4,Nico Cellinese,Acanthella pulchra,8/8/12,37.3,-120.4,Nico Cellinese
```

Then we'll then MapReduce over it to create some outputs.

### First output
The type output lists identifiers and what type they are. `type.csv` would look like this:

```
ID,Type
ark:/9999/fk411,dwc:Taxon
ark:/9999/fk412,dwc:Taxon
ark:/9999/fk413,dwc:Taxon
ark:/9999/fk414,dwc:Taxon
ark:/9999/fk421,dwc:Locality
ark:/9999/fk422,dwc:Locality
ark:/9999/fk423,dwc:Locality
ark:/9999/fk424,dwc:Locality
ark:/9999/fk431,dwc:Occurrence
ark:/9999/fk432,dwc:Occurrence
ark:/9999/fk433,dwc:Occurrence
ark:/9999/fk434,dwc:Occurrence
```

### Second output

The `source_of.csv` describes the relationships between the identifiers. In this example, dwc:Locality source_of dwc:Occurrence source_of dwc:Taxon:

```
source	target
ark:/9999/fk421,ark:/9999/fk431
ark:/9999/fk431,ark:/9999/fk411
ark:/9999/fk422,ark:/9999/fk432
ark:/9999/fk432,ark:/9999/fk412
ark:/9999/fk423,ark:/9999/fk433
ark:/9999/fk433,ark:/9999/fk413
ark:/9999/fk424,ark:/9999/fk434
ark:/9999/fk434,ark:/9999/fk414
```

## Goal 2

Bulk load files to CartoDB

## Goal 3

Write a query API and client, then construct a query against that in Java against the data in the AppEngine datastore for a single UUID as a query parameter that returns an N3 file containing `source_of` relationships and types.  For example:

```java
public interface Query {
  public static String execute(String uuid);
}
```


Would return objects and derivitives.

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
