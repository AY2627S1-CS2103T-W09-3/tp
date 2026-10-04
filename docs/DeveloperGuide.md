---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a property agent who manages multiple buyers and sellers simultaneously
* needs to retrieve client information quickly while handling calls, viewings, travel, and follow-ups
* needs to record clients' contact details, budgets, property preferences, viewing information, and follow-up status
* works primarily on a desktop or laptop
* prefers typing and keyboard-driven workflows over navigating with a mouse
* is comfortable using a Command Line Interface (CLI)
* may use their device where clients or colleagues can see the screen and therefore needs to protect sensitive client information

**Value proposition**: EstateBookUltraProMax gives property agents fast, CLI-optimised access to buyer and seller
contacts, budgets, property preferences, viewing information, and follow-up statuses. It removes the friction of
managing clients through spreadsheets by allowing records to be added, updated, and retrieved quickly using
keyboard-driven commands.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​ | I want to …​ | So that I can…​ |
| -------- | ------- | ------------ | --------------- |
| `* * *` | property agent | add a buyer contact with essential contact details | record a new buyer lead before I forget it |
| `* * *` | property agent | add a seller contact with essential contact details | record a new seller lead before I forget it |
| `* * *` | property agent | view all recorded contacts | see the clients currently under my care |
| `* * *` | property agent | view a selected client's full details | quickly understand the client's information and requirements |
| `* * *` | property agent | edit a client's contact details | keep the client's record accurate when their details change |
| `* * *` | property agent | delete a wrongly created contact | remove records that should not exist |
| `* * *` | property agent | find a client using one or more whole words from the client's name | locate the client's details without going through the entire list |
| `* * *` | property agent | find a client using their phone number or email address | identify an incoming caller or message quickly |
| `* * *` | property agent | record whether a client is a buyer, seller, or both | understand my relationship with the client at a glance |
| `* * *` | property agent | record a client's address | retain the client's important contact information |
| `* * *` | property agent | record a buyer's budget range | avoid proposing properties outside the buyer's means |
| `* * *` | property agent | record a buyer's preferred locations | shortlist properties in areas acceptable to the buyer |
| `* * *` | property agent | record a buyer's preferred property type | exclude unsuitable property categories |
| `* * *` | property agent | record a buyer's minimum bedroom requirement | avoid recommending properties that are too small |
| `* * *` | property agent | record notes about a client | retain important context that does not fit into the standard fields |
| `* * *` | property agent | tag contacts using meaningful labels | group clients according to attributes relevant to my work |
| `* * *` | property agent | record a viewing appointment for a client | remember the client's upcoming property viewing |
| `* * *` | property agent | view a client's viewing status | quickly determine whether a viewing has been scheduled |
| `* * *` | property agent | update a viewing's status | keep the client's viewing information current |
| `* * *` | property agent | exit the application safely | ensure that my latest changes are retained |
| `* *` | new property agent | view realistic sample clients | understand what information the application can manage |
| `* *` | new property agent | view concise help within the application | learn the available commands without leaving my workflow |
| `* *` | user ready to begin | clear all sample or experimental data | start with a clean client list |
| `* *` | property agent | filter contacts by buyer or seller role | focus on the relevant side of my client pipeline |
| `* *` | property agent | filter buyers by budget range | identify buyers who may be suitable for a property |
| `* *` | property agent | filter buyers by preferred location | identify buyers interested in a particular area |
| `* *` | property agent | view a client's upcoming viewings | prepare for the client's next appointment |
| `* *` | busy property agent | view all appointments for a selected day | plan my schedule and avoid missing appointments |
| `* *` | property agent | record feedback after a viewing | remember the client's response when choosing the next recommendation |
| `* *` | property agent | record a client's current follow-up status | know what action I need to take next |
| `* *` | property agent | record the next follow-up date | contact the client at an appropriate time |
| `* *` | property agent | view clients whose follow-ups are due or overdue | prioritise clients who require my attention |
| `* *` | property agent | sort contacts by name, latest update, or next follow-up | review my clients in an order suitable for my current task |
| `* *` | property agent | undo my most recent data-changing action | recover quickly from an input mistake |
| `* *` | property agent | detect possible duplicate contacts | avoid splitting one client's information across multiple records |
| `* *` | long-time property agent | archive an inactive client | reduce clutter without permanently losing past information |
| `* *` | returning property agent | restore an archived client | resume working with a past client who has become active again |
| `* *` | property agent migrating from spreadsheets | import client records from a common tabular format | avoid entering all my existing client information manually |
| `* *` | privacy-conscious property agent | mask sensitive client details on screen | work safely when other people can see my computer |
| `* *` | property agent | identify incomplete client records | fill important gaps before they affect my service |
| `*` | fast-typing property agent | use short aliases for common commands | reduce typing during repetitive work |
| `*` | property agent | reuse a recent command with small changes | process similar client updates efficiently |
| `*` | property agent | preview the records affected by a bulk command | avoid modifying the wrong clients |
| `*` | property agent | merge confirmed duplicate contacts | maintain one complete record for each client |
| `*` | property agent changing agencies | export selected client records | retain records that I am permitted to take with me |
| `*` | property agent | create a backup of my client records | reduce the risk of losing important information |
| `*` | property agent | restore my client records from a valid backup | recover my information after data loss or device replacement |
| `*` | property agent | view when and how a client record was last changed | determine whether the information is current |
| `*` | property agent | separate personal leads from agency-assigned leads | keep my different work contexts organised |
| `*` | property agent | record relationships between clients | recognise couples, co-buyers, family members, and co-owners |
| `*` | property agent | save a frequently used search | repeat a common client review without rebuilding the search criteria |

### Use cases

For all use cases below, the **System** is **EstateBookUltraProMax** and the **Actor** is a **property agent**, referred to as the **Agent**.

**MSS** stands for **Main Success Scenario**.

#### UC01: Register and classify a new client

**MSS**

1. Agent requests to add a client, providing the client's name, phone number, email address, address, and any optional tags.
2. EstateBookUltraProMax adds the client and displays the recorded details.
3. Agent requests to assign the new client a role of buyer, seller, or both.
4. EstateBookUltraProMax assigns the role and displays the client's updated role.

   Use case ends.

**Extensions**

* 1a. Required details are missing or one or more supplied values are invalid.
  * 1a1. EstateBookUltraProMax displays an error describing the invalid or missing details without adding the client.

  Use case resumes at step 1.

* 1b. The supplied name, phone number, or email address matches an existing client's.
  * 1b1. EstateBookUltraProMax identifies the conflicting field and rejects the addition.

  Use case resumes at step 1.

* 3a. The specified client reference is invalid.
  * 3a1. EstateBookUltraProMax displays an error without assigning a role.

  Use case resumes at step 3.

* 3b. The role is missing or is not buyer, seller, or both.
  * 3b1. EstateBookUltraProMax displays an error indicating the permitted roles.

  Use case resumes at step 3.

#### UC02: Update a client's contact details

**MSS**

1. Agent requests to find a client using one or more whole words from the client's name.
2. EstateBookUltraProMax displays a list of matching clients.
3. Agent identifies a client from the displayed results and requests changes to the client's contact details.
4. EstateBookUltraProMax updates the specified details and displays the updated client.

   Use case ends.

**Extensions**

* 1a. No search keyword is supplied.
  * 1a1. EstateBookUltraProMax displays an error indicating that a search keyword is required.

  Use case resumes at step 1.

* 2a. No clients match the search keywords.

  Use case ends.

* 3a. The specified client reference is invalid.
  * 3a1. EstateBookUltraProMax displays an error without updating the client.

  Use case resumes at step 3.

* 3b. No changes are supplied, or one or more supplied values are invalid.
  * 3b1. EstateBookUltraProMax displays an error without updating the client's details.

  Use case resumes at step 3.

* 3c. The updated name, phone number, or email address would match another client's details.
  * 3c1. EstateBookUltraProMax displays an error identifying the conflict and rejects the update.

  Use case resumes at step 3.

#### UC03: Change a client's role

**MSS**

1. Agent requests to list all clients.
2. EstateBookUltraProMax displays the clients and their assigned roles.
3. Agent identifies a client from the displayed list and requests to change the client's role to buyer, seller, or both.
4. EstateBookUltraProMax updates the client's role and displays the updated client.

   Use case ends.

**Extensions**

* 2a. The client list is empty.

  Use case ends.

* 3a. The specified client reference is invalid.
  * 3a1. EstateBookUltraProMax displays an error without updating the client's role.

  Use case resumes at step 3.

* 3b. The role is missing or is not buyer, seller, or both.
  * 3b1. EstateBookUltraProMax displays an error indicating the permitted roles without updating the client's role.

  Use case resumes at step 3.

#### UC04: Delete a client

**MSS**

1. Agent requests to list all clients.
2. EstateBookUltraProMax displays the client list.
3. Agent requests to delete a specific client from the displayed list.
4. EstateBookUltraProMax deletes the client, identifies the deleted client, and displays the updated list.

   Use case ends.

**Extensions**

* 2a. The client list is empty.

  Use case ends.

* 3a. The specified client reference is missing or invalid.
  * 3a1. EstateBookUltraProMax displays an error without deleting any client.

  Use case resumes at step 3.

#### UC05: Record a client's viewing outcome

**MSS**

1. Agent requests to view a client's viewing records.
2. EstateBookUltraProMax displays the client's viewing records and their statuses.
3. Agent identifies a viewing and provides the client's feedback and an updated viewing status.
4. EstateBookUltraProMax records the feedback and status and displays the updated viewing record.

   Use case ends.

**Extensions**

* 1a. The specified client does not exist.
  * 1a1. EstateBookUltraProMax displays an error without displaying any viewing records.

  Use case resumes at step 1.

* 2a. The client has no recorded viewings.

  Use case ends.

* 3a. The specified viewing does not exist among the client's viewing records.
  * 3a1. EstateBookUltraProMax displays an error without changing any viewing records.

  Use case resumes at step 3.

* 3b. The supplied viewing status is invalid.
  * 3b1. EstateBookUltraProMax displays an error indicating the permitted statuses without changing the viewing records.

  Use case resumes at step 3.

#### UC06: Update a client's follow-up plan

**MSS**

1. Agent requests to list clients whose follow-ups are due or overdue.
2. EstateBookUltraProMax displays the matching clients and their recorded follow-up details.
3. Agent identifies a client and provides an updated next action and follow-up date.
4. EstateBookUltraProMax records the updated follow-up plan and displays the updated details.

   Use case ends.

**Extensions**

* 2a. No clients have follow-ups that are due or overdue.

  Use case ends.

* 3a. The specified client reference is invalid.
  * 3a1. EstateBookUltraProMax displays an error without changing any follow-up plan.

  Use case resumes at step 3.

* 3b. The next action is missing or the follow-up date is invalid.
  * 3b1. EstateBookUltraProMax displays an error describing the missing or invalid details without changing the follow-up plan.

  Use case resumes at step 3.

### Non-Functional Requirements

1. **Usability:** A user with an above average typing speed for English text should be able to accomplish most tasks using the _Command Line Interface (CLI)_ and command _aliases_ than navigating with mouse-driven GUI.
2. **Environment:** The system should work on any _mainstream OS_ as long as it has Java `25` installed. 
3. **Performance:** The system should be able to hold up to <ins>100</ins> active client records without any noticeable sluggishness in performance during typical usage (e.g., searching, filtering).
4. **Data Reliability:** The application must ensure that all data-changing actions are saved safely so that the latest changes are retained upon exiting.
5. **Fault Tolerance:** The application must gracefully handle invalid inputs (e.g., invalid client references, missing roles, or conflicting duplicate details) by displaying clear error messages, instead of crashing.


### Glossary

| Term                             | Definition                                                                                                                                                                         |
|:---------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **CLI (Command Line Interface)** | A text-based user interface where the agent interacts with the application by typing commands, optimised for speed and keyboard-driven workflows.                                  |
| **Mainstream OS**                | Standard desktop and laptop operating systems, specifically Windows, Linux, Unix, or macOS.                                                                                        |
| **Client Role**                  | The classification of a client's relationship with the agent. A client can be classified as a `Buyer`, a `Seller`, or `Both`.                                                      |
| **Viewing**                      | A scheduled appointment for a client to visit a property. It contains a status (e.g., `pending`, `completed`).                                                                     |
| **Tag**                          | A custom, meaningful label assigned to a client to group them according to specific attributes relevant to the agent's work.                                                       |
| **Alias**                        | A shortened, memorable version of a standard CLI command used to reduce typing overhead for fast-typing agents.                                                                    |
| **Duplicate Contact**            | A client record that shares identical identifying fields (such as phone number or email) with another record, which the system can detect to prevent fragmented information.       |
| **Property Preferences**         | A collective term for a buyer's specific real estate requirements, including their budget range, preferred locations, property type, and minimum bedroom requirements.             |
| **Sample Data**                  | A set of realistic, pre-loaded dummy client records provided to help new agents explore and learn the application's features safely before clearing it to start their actual work. |

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
