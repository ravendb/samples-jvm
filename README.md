# RavenDB JVM Client Tutorial  
[RavenDB](ravendb.net) is a lightning fast #NoSQL document database. It's fully ACID, high-availability, 
multi-platform, and open-source. It supports clients in a variety of programming languages including Java.  

This tutorial is a quick introduction to RavenDB and the JVM Client API. As you'll see, RavenDB is very easy to deploy and 
administer. We will be looking at a simple demo hospital management app as an example.  

The demo app was built using the [Vaadin Flow](https://vaadin.com/flow) framework, which allows you to develop web apps 
using only Java.  

Contents:
* [How to Install RavenDB Community Edition](./README.md#how-to-install-ravendb-community-edition)
* [How to Run the Demo](./README.md#how-to-run-the-demo)
* [Entities as Documents](./README.md#entities-as-documents)
* [Document Store](./README.md#document-store)
* [Session and Unit of Work Pattern](./README.md#session-and-unit-of-work-pattern)
* [CRUD operations](./README.md#crud-operations)
* [Paging on large record sets](./README.md#paging-through-large-record-sets)
* [BLOB handling - attachments](./README.md#blob-handling---attachments)
* [Queries](./README.md#queries)

## How to Install RavenDB Community Edition  

1. [Register a free community license](https://ravendb.net/buy)  
2. [Download and unzip the RavenDB version 4.x server package](https://ravendb.net/download) (the latest stable version)  
3. From command line, run the `.\run.ps1` script found in the package root (or `.\setup-as-service.ps1` to launch as a service)  
4. Once installed, the [Setup Wizard](https://ravendb.net/docs/article-page/4.2/java/start/installation/setup-wizard) will
launch on a browser and prompt you to choose a security mode. "Unsecured" mode is appropriate for local development and for
the purposes of this tutorial.

![Setup security](/screenshots/setup_security.png)

5. Leave the other options on default. At the end of the Setup Wizard, you'll be prompted to restart 
the server. Once you do, the [RavenDB Management Studio](https://ravendb.net/docs/article-page/4.2/java/studio/overview) 
will launch on `localhost:8080`. (The Studio is always available at the designated http port while a Server instance is running. 
You can also access the Studio from  the Server's command line interface with command `openbrowser`)  

6. Once in the Studio, open the `About` tab to register your license:

![Register License](/screenshots/manage-license-1.png)

More detailed installation and setup instructions can be found in [RavenDB's online documentation](https://ravendb.net/docs/article-page/4.2/java/start/getting-started).  

## How to run the demo
1. Fetch the code sources for this project with:  
```
$ git clone https://github.com/ravendb/ravendb-jvm-tutorials.git
```
2. From the Studio, click on the `Database` tab and create a new database with the name "Hospital".  
3. Populate this database by going to `Settings` > `Import Data` and importing the file `Hospital.ravendbdump` found in 
the project root:  

![Import Data](/screenshots/ravendbdump.png)

4. For quick and easy testing, we recommend launching the app on a jetty server. The maven jetty plugin is already included 
in `pom.xml`. In your IDE, create a new maven run configuration and set `jetty:run` as the goal.  

When run, the web app will be available at `http://localhost:8889/`:
![App Homepage](/screenshots/p_home.png)  

### Demo Folder Structure  
This app implements the [Model-View-Presenter pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter).
The `model` folder contains the type definitions of the entities that we will be storing on the server, plus some miscellaneous 
types. The `view` folder contains the Vaadin components and layouts. The `presenter` folder contains the presenters 
which are responsible for communication with the RavenDB server.  

## Entities as Documents
As a NoSQL (or non-relational) database, RavenDB doesn’t represent entities as rows in a table with a rigid schema. 
Instead, it represents them as _"documents"_ with no constraints on their structure. Similar documents are grouped in 
_collections_. Each document contains data, a unique id, and metadata in JSON format.  

The model for this demo consists of 3 main entities: _Patients_, _Doctors_, _Conditions_. There are also _Visits_, 
appointments between one doctor and one patient. Visits are embedded as an array within the corresponding patient entity. 
These three entity types are represented by three collections of documents on the Server.

![UML Diagram](/screenshots/uml.png)

Below are the type definitions of these entities on the Client side (getters and setters are omitted for brevity), accompanied by 
examples of specific documents in JSON.  

<details><summary>Click to expand POJOs and documents</summary>
1. The Patient entity:  

```java
public class Patient {
    private String id;
    private String firstName,lastName;
    private Date birthDate;
    private Gender gender;

    private String email;
    private Address address;
    private List<Visit> visits;
}
```
A JSON document of an example Patient containing an array of Visits:  

```JSON
{
    "firstName": "Megi",
    "lastName": "Devasko",
    "birthDate": "2016-11-30T22:00:00.0000000Z",
    "gender": "FEMALE",
    "email": "sss@box.com",
    "address": null,
    "visits": [
        {
            "date": "2019-02-26T22:00:00.0000000Z",
            "doctorId": "doctors/1-A",
            "type": "HOUSE",
            "visitSummary": "just a minor pain",
            "conditionId": "conditions/1-A",
            "doctorName": "Dr. Megan Austin"
        },
        {
            "date": "2019-01-31T22:00:00.0000000Z",
            "doctorId": "doctors/2-A",
            "type": "EMERGENCY ROOM",
            "visitSummary": "nothing to worry about",
            "conditionId": "conditions/2-A",
            "doctorName": "Dr. Megalo Karimov"
        }
    ],
    "@metadata": {
        "@collection": "Patients",
        "@flags": "HasAttachments",
        "@id": "patients/33-A",
        "Raven-Java-Type": "net.ravendb.demo.model.Patient"
    }
}
```
2. Visit entity:  
```java
public class Visit {
    private Date date;
    private String doctorId;
    private Type type;
    private String visitSummary;
    private String conditionId;
    private String doctorName;
}
```
On the server side this entity is represented as an array within a Patient document - see example Patient above.  

3. Condition entity:  
```java
public class Condition {
    private String id;
    private String name;
    private String symptoms;
    private String recommendedTreatment;
}
```
Example Condition:  
```JSON
{
    "name": "Diabetes",
    "symptoms": "swollen legs, impaired eyesight",
    "recommendedTreatment": "sugar-free diet",
    "@metadata": {
        "@collection": "Conditions",
        "@id": "conditions/6-A",
        "Raven-Java-Type": "net.ravendb.demo.model.Condition"
    }
}
```

4. Doctor entity:  
```java
public class Doctor {
    private String id;
    private String name;
    private String department;
    private int age;
}
```
Example Doctor:  
```JSON
{
    "name": "Sergiz Ovesian",
    "department": "LV",
    "age": 45,
    "@metadata": {
        "@collection": "Doctors",
        "@id": "doctors/1-A",
        "Raven-Java-Type": "net.ravendb.demo.model.Doctor"
    }
}
```
</details>

Rather than picking ids for our documents, we will leave them `null`. RavenDB automatically generates 
unique and human readable ids in the format: `[collection document belongs to]/[HiLo number tag]-[RavenDB cluster node tag]`. 
The [HiLo Algorithm](https://ravendb.net/docs/article-page/4.2/java/client-api/document-identifiers/hilo-algorithm) 
provides the unique portion of the id. We will be using one Server - effectively a one-node cluster - and by default its tag 
is "A", so patient documents have the ids `patients/1-A`,  `patients/2-A` and so on.

## Document Store
The Java Client is included as a dependency in `pom.xml`.  
```xml
<dependency>
    <groupId>net.ravendb</groupId>
    <artifactId>ravendb</artifactId>
    <version>LATEST</version>
</dependency>
```
The primary object of the Client API is the _Document Store_, which manages the application's connection with the Server 
and holds various configuration options. One of these options is whether to use optimistic concurrency control, which 
we'll want to enable for this app. It is recommended that you create just one Document Store instance per application by 
using the [singleton pattern](https://www.javaworld.com/article/2073352/core-java-simply-singleton.html) as demonstrated 
below:  
```java
public final class RavenDBDocumentStore {
    private static IDocumentStore store;

    static {

        //Create new Document Store with the url of the RavenDB Server
        //and with `Hospital` set as the default database
        store = new DocumentStore(new String[]{"http://127.0.0.1:8080"},
                                  "Hospital");

        //Edit the conventions to enable optimistic concurrency
        DocumentConventions conventions = store.getConventions();
        conventions.setUseOptimisticConcurrency(true);

        store.initialize();
    }

    //Return the single Document Store instance 
    public static IDocumentStore getStore() {
        return store;
    }

}
```
## Session and Unit of Work Pattern  

Any operation we want to perform starts by obtaining a new _Session_ object from the Document Store.
The Session implements the [Unit of Work](https://martinfowler.com/eaaCatalog/unitOfWork.html)
pattern. This has several implications:  
* The Session batches requests to the server to minimize the number of round trips over the network.  
* The Session tracks changes for all the entities that it has either loaded from or stored to the database, and commits 
them as an ACID transaction when `[session].saveChanges()` is called.  
* A single document always resolves to the same instance - i.e. if we try to load a document twice, the second call 
will load the entity from a local cache rather than going over the network.  

In this demo, sessions are created and released in response to the `attach` and `detach` events in the lifecycle of each 
of our `views` (which inherit from Vaadin's `VerticalLayout`). We create a session when we navigate to a page in our web 
app (not including the root URL, or menu page), and release it when that page is closed. Vaadin maintains page state persistence, 
a feature which will help us to minimize calls to the Server.  

In the view:  
```java
@Override
protected void onAttach(AttachEvent attachEvent) {
    presenter.openSession();
    loadPage();
}

@Override
protected void onDetach(DetachEvent detachEvent) {
    presenter.releaseSession();
    super.onDetach(detachEvent);
}
```
In the presenter:  
```java
public void openSession() {
    if (session == null) {
        session = RavenDBDocumentStore.getStore().openSession();
    }
}

public void releaseSession() {
    session.close();
}
```
## CRUD operations  
Now we can look at how the demo app implements the basic CRUD functions for our patient data. These functions are located 
in the `PatientPresenter`, an instance of which is held by the `PatientView`.  

The **create** operation:  
```java
public void create(Patient patient, ProfilePicture profilePicture) {

    session.store(patient);
    
    //Attachment handling
    if (profilePicture != null) {
        session.advanced().attachments().store(patient, ATTACHMENT_NAME,
                                               profilePicture.getInputStream());
    }

    session.saveChanges();
}
```
Creates a new Patient document and uploading it to the Server. Generally documents only need `session.store()` and `session.saveChanges()`
to be stored. In this case the patient has a profile picture which doesn't fit in JSON format, so 
it needs to be handled separately as an _attachment_. Attachments are discussed further [below](./README.md#blob-handling---attachments).  

The **update** operation:  
```java
public void update(Patient patient, ProfilePicture profilePicture) throws ConcurrencyException {
    session.store(patient);

    if (profilePicture != null) {
        session.advanced().attachments().store(patient.getId(), ATTACHMENT_NAME,
                                               profilePicture.getInputStream());
    }
    
    session.saveChanges();
}
```
Receives a patient and profile picture which have already been modified and stores them, in almost the same way as `create()`.

The **delete** operation:
```java
public void delete(Patient patient) {
    session.delete(patient);
    session.saveChanges();
}
```
Deletes a given patient. `delete()` can take either an object instance, as shown here, or a document id.

To **read** documents, we simply load them from the Server using 
```java
session.load([patientId])
```

## Paging Through Large Record Sets
Paging through large amounts of data is one of the most common operations in RavenDB.  
Let's say we need to display results in batches on a lazy loading grid. In this app, the grid is configured to obtain the
total amount of results to a query (to calculate the total number of pages) and then to lazily load the results in batches 
of 10 as the user navigates from page to page. We can use `statistics()` to access useful data about the query, including 
the total number of results. For the patients grid, the corresponding attachments are also obtained and streamed into a 
byte array.

![Patient Paging](/screenshots/p_paging.png)

`getPatientsList()`, with error handling omitted for brevity:
```java
public Pair<Collection<PatientWithPicture>, Integer> getPatientsList(int offset, int limit, boolean order) {
    Reference<QueryStatistics> statsRef = new Reference<>();
    IDocumentQuery<Patient> query = session.query(Patient.class)
                                           .skip(offset)
                                           .take(limit)
                                           .statistics(statsRef);

    if (order) {
        query.orderBy("birthDate");
    }

    Collection<Patient> list = query.toList();
    int totalResults = statsRef.value.getTotalResults();

    Collection<PatientWithPicture> patientWithPictures = new ArrayList<>();
}
```

## BLOB Handling - Attachments
Binary data that cannot be stored as JSON (such as images, audio, etc.) can be associated with a document as one or more _attachments_.
An example are the profile pictures of Patients. This POJO represents attachments on the client side:
```java
public class Attachment {

    String name;
    byte[] bytes;

    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public StreamResource getStreamResource() {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return new StreamResource(name, () -> bis);
    }
}
```
(Getters and setters are omitted for brevity)

Attachments can be loaded and edited separately from the document, which saves sending the entire attachment over the network
every time a document is accessed. Like documents, attachments are tracked by the Session and will be included in the same 
ACID transaction as any other changes tracked by the same Session.

This method retrieves the profilePicture for a given patient document:
```java
public ProfilePicture getProfilePicture(Patient patient) {
    ProfilePicture profilePicture = new ProfilePicture();

    try (CloseableAttachmentResult result = session.advanced()
            .attachments()
            .get(patient.getId(), ATTACHMENT_NAME)) {


        if (result == null)
            return null;
        InputStream data = result.getData();
        byte[] bytes = IOUtils.toByteArray(data);
        profilePicture.setBytes(bytes);
        profilePicture.setName(ATTACHMENT_NAME);
        
        return profilePicture;
    } catch (IOException e) {
        logger.log(Level.SEVERE, "", e);
    }

    return null;
}
```

## Queries
In RavenDB, a query can only be satisfied by an index. You can create your own indexes, but if no appropriate index exists, 
RavenDB will automatically create one on the fly. Indexes learn from each new query, and are continuously optimized to 
satisfy all previous queries made to a given collection. In this way RavenDB is built to optimize the speed of reads with minimal 
administration. (By default, indexes are updated asynchronously so that they don't impact write speed. In most use cases 
this improves performance, but you can always have write operations wait for the index to update before returning using 
`session.advanced().waitForIndexesAfterSaveChanges();`.)  

Queries are created by chaining methods on `session.query()`, and executed by calling `.toList()`. Before being sent to the server, every
query is translated to [RQL (RavenDB Query Language)](https://ravendb.net/docs/article-page/4.2/java/indexes/querying/what-is-rql) - 
our SQL-like language. RQL was designed to expose RavenDB's query pipeline to the user. You can write your queries in RQL
using `session.advanced().rawQuery([RQL string])`. Any query can be converted to RQL by calling `.toString()`. Here are 
some example queries constructed in Java, accompanied by the equivalent RQL:

### 1. Retrieve all documents from a collection
```java
IDocumentQuery<Doctor> query = session.query(Doctor.class);
```
This is the simplest possible query, with no filtering, paging, or projection. The parameter `Doctor.class` indicates the
type of the entities retrieved, and also that the collection being queried is `Doctors`.

Equivalent RQL:
```SQL
from Doctors
```

### 2. Paging and query statistics
We have a grid that displays 10 patients per page. To display the third page, we tell
our query to `.skip()` the first 20 documents and `.take()` the next 10.  

```java
Reference<QueryStatistics> myQueryStats = new Reference<>();
IDocumentQuery<Patient> query = session.query(Patient.class)
                                       .skip(20)
                                       .take(10)
                                       .statistics(myQueryStats)

Collection<Patient> myPatients = query.toList();
int totalResults = myQueryStats.value.getTotalResults();
```

We also want to know how many pages there are in total so we can render the page selection buttons. Some useful data, called the
_query statistics_, are automatically sent to the client along with the response to each query. To save them we 
call `.statistics(myQueryStats)` and then we can access the total number of results with `myQueryStats.value.getTotalResults()`.

Equivalent RQL:
```SQL
from Patients
limit 10 offset 20
```
`limit` corresponds to `.take()` and `offset` corresponds to `.skip()`. There is no command in RQL regarding query statistics,
since they are always sent regardless.

### 3.Filtering and including related documents  
RavenDB does not use joins, like SQL databases do. Instead, if one document contains the id of another document, that other
document can be retrieved by the same query .  
This query filters the Patients collection to retrieve only patients with the full name "John Doe", and includes the doctor documents
whose ids are listed in that patient's visits.  

```java
Collection<Patient> myPatients = session.query(Patient.class)
                                        .whereEquals("firstName", "John")
                                        .andAlso()
                                        .whereEquals("lastName", "Doe")
                                        .include("visits[].doctorId")
                                        .toList();

// Assume that John Doe has an appointment with `doctors/1-A`.
// `doctors/1-A` can now be loaded from a local cache rather 
// than by making an additional round trip to the server.
Doctor myDoctor = session.load("doctors/1-A");
```

Equivalent RQL:
```SQL
from Patients
where firstName = 'John' and lastName = 'Doe'
include 'visits[].doctorId'
```
Although the above raw query works as it is written, if you were to take the method chain form of the query and call 
`toString()`, the result will be:
```SQL
from Patients
where firstName = $p0 and lastName = $p1
include 'visits[].doctorId'
```
Where `$p0` and `$p1` represent parameters which are sent to the server along with the query, but not in the body of the RQL.

### 4. Aggregating and projecting  
```java
List<DoctorVisit> results = session.query(Patient.class)
                                   .groupBy("visits[].doctorId")
                                   .selectKey("visits[].doctorId", "doctorId")
                                   .selectCount()
                                   .whereNotEquals("doctorId", null)
                                   .orderByDescending("count")
                                   .ofType(DoctorVisit.class)
                                   .include("visits[].doctorId")
                                   .toList();
```
In this query from the demo, we want to rank doctors by the number of visits they have scheduled with patients.  
`groupBy("visits[].doctorId")` performs a map-reduce operation on the `Patients` collection, grouping by the doctorIds 
listed in the visits array.  

However, since we don't want any of the other data in the patient documents, it would be a waste to send it over the network. 
With `.selectKey("visits[].doctorId", "doctorId")` we retrieve only the doctorIds and with `.selectCount()`, we retrieve the 
number of visits per doctor.  

`ofType(DoctorVisit.class)` takes our results and casts them to the type `DoctorVisit`.

Equivalent RQL:
```SQL
from Patients
group by visits[].doctorId
where doctorId != null
order by count desc
select visits[].doctorId as doctorId, count() as count
include 'visits[].doctorId'
```
