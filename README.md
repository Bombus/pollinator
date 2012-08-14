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




A CSV file called `source_of.csv` that contains three UUIDs: 

1. A UUID for the row itself (primary key)
2. UUID of a source object
3. UUID of a target object (The source is the `source_of` the target)


### First output
The type output lists identifiers and what type they are.  `type.csv` would look like this:

```
ID  Type
1	dwc:Taxon
2	dwc:Taxon
3	dwc:Taxon
4	dwc:Taxon
5	dwc:Taxon
6	dwc:Taxon
7	dwc:Taxon
8	dwc:Taxon
9	dwc:Locality
10	dwc:Locality
11	dwc:Locality
12	dwc:Locality
13	dwc:Locality
14	dwc:Locality
15	dwc:Locality
16	dwc:Locality
17	dwc:Occurrence
18	dwc:Occurrence
19	dwc:Occurrence
20	dwc:Occurrence
21	dwc:Occurrence
22	dwc:Occurrence
23	dwc:Occurrence
24	dwc:Occurrence

```

### Second output

The `source_of.csv` would look like this:

```
pk  source	target
100	9	17
101	17	1
102	10	18
103	18	2
104	11	19
105	19	3
106	12	20
107	20	4
108	13	21
109	21	5
110	14	22
111	22	6
112	15	23
113	23	7
114	16	24
115	24	8
```

### Third output

The `related_to.csv` would look like:

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
