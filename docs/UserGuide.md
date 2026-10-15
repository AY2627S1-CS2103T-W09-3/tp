---
layout: page
title: User Guide
---

EstateBookUltraProMax is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, EstateBookUltraProMax can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-W09-3/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for EstateBookUltraProMax.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar estatebookultrapromax.jar`.<br>
   A GUI similar to the one below should appear in a few seconds.<br>
   If no saved data file exists, the app starts with five sample clients:
   two buyers, two sellers, and one who is both.
   Otherwise, it loads your saved clients.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all clients.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a client named `John Doe` to EstateBookUltraProMax.

   * `delete 3` : Deletes the 3rd client shown in the current list.

   * `clear` : Deletes all clients.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Prefixed fields in `add` and `edit` can appear in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Input rules

These rules apply when adding or editing clients.

| Field | Rules |
|---|---|
| Name | Letters, numbers and spaces only; cannot be blank. Hyphens and apostrophes are not accepted. |
| Phone | At least 3 digits; no spaces, letters or symbols. |
| Email | Use `local-part@domain`. See the email rules below. |
| Address | Cannot be blank. |
| Tag | Letters and numbers only; no spaces or symbols. |

Email rules:

* Before `@`, use letters, digits and the symbols `+`, `_`, `.`, `-`.
* These symbols cannot appear at the start or end, or consecutively.
* Domain labels use letters, digits and internal hyphens, separated by dots. The final label must have at least 2 characters.

Prefix rules:

* Each of `n/`, `p/`, `e/` and `a/` can appear only once per command.
* Repeat `t/` to supply multiple tags, such as `t/urgent t/firsttime`.
* For `edit`, use an empty `t/` to remove all tags.

### Duplicate clients

An `add` or `edit` command is rejected if it would match another client's:

* Name, ignoring case.
* Phone number exactly.
* Email address exactly, including letter case.

The existing records remain unchanged when the command is rejected.

For different clients with the same name, add a distinguishing word, such as
`Tan Wei Ming Bedok` and `Tan Wei Ming Tampines`. Their phone numbers and email
addresses must also be different.

For couples or co-buyers sharing contact details, keep one household record
and add the tag `t/couple`. Adding this tag does not allow duplicate records.

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage(1).png)

Format: `help`


### Adding a client: `add`

Adds a client to the EstateBookUltraProMax.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A client can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all clients: `list`

Shows all clients and clears the current search filter.

After using `find`, enter `list` to restore the complete client list.
For `edit`, `delete` and `role`, always use the index currently shown beside
the client. Indexes can change when the displayed list changes.

Format: `list`

### Editing a client: `edit`

Edits an existing client in EstateBookUltraProMax.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the client at the specified `INDEX`. The index refers to the index number shown in the displayed client list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the client's existing tags are removed; adding tags is not cumulative.
* To remove all of a client's tags, enter `t/` without a tag after it.
* After a successful edit, the list shows all clients again.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st client to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd client to be `Betsy Crower` and clears all existing tags.

### Assigning a client's role: `role`

Marks a client as a buyer, a seller, or both.

Format: `role INDEX ROLE`

* `INDEX` must be a positive integer referring to a client in the displayed list.
* `ROLE` accepts `buyer`, `seller`, or `both`, ignoring case.
* The client card displays `Buyer`, `Seller`, or `Buyer, Seller`.
* Newly added clients have no role assigned.
* Assigning a role replaces any existing role.
* After a successful role change, the list shows all clients again.

Examples:

* `role 1 buyer` marks the first displayed client as a buyer.
* `role 2 both` marks the second displayed client as both a buyer and a seller.
  The role badge displays `Buyer, Seller`.

For example, if the first displayed client is John Doe, `role 1 buyer` displays:
`Role updated: John Doe is now marked as Buyer`

### Finding clients: `find`

Finds clients by name, phone number, or email address.

Format: `find KEYWORD [MORE_KEYWORDS]`

* Name searches match whole words, ignoring case. For example, `john` matches `John Doe`, but `Joh` does not.
* Phone searches require the complete phone number.
* Email searches require the complete email address, ignoring case.
* Partial phone numbers and partial email addresses do not match.
* Multiple keywords use OR matching: a client appears if any keyword matches their name, phone number, or email.
* Keyword order does not matter.
* Entering `find` without keywords shows an error and leaves the displayed list unchanged.
* Use `list` to show all clients again.
* If no clients match, the result shows `No clients found`.
* If one client matches, the result shows `1 client listed!`.
* If multiple clients match, the result shows the number of matches,
  e.g. `2 clients listed!`.

Examples:

* `find John` finds clients with the whole word John in their names.
* `find 98765432` finds clients with that exact phone number.
* `find johnd@example.com` finds clients with that email address.
* `find John 98765432` finds clients matching either the name word John or the phone number 98765432.

### Deleting a client: `delete`

Deletes the specified client from EstateBookUltraProMax.

Format: `delete INDEX`

* Deletes the client at the specified `INDEX`.
* The index refers to the index number shown in the displayed client list.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:
* `list` followed by `delete 2` deletes the 2nd client in EstateBookUltraProMax.
* `find Betsy` followed by `delete 1` deletes the 1st client in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from EstateBookUltraProMax.

Format: `clear`

<div markdown="span" class="alert alert-warning">:exclamation: **Warning:**
`clear` deletes every client, including clients hidden by a search
filter. It runs immediately without confirmation and cannot be undone within
the app. Back up your data before using it.
</div>

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

EstateBookUltraProMax automatically saves data after successful commands.
You do not need to save manually.

Data is stored in `data/addressbook.json`, relative to the folder from which
you launch the application (the working directory).

If you follow Quick Start and launch from the folder containing the JAR,
the `data` folder will be created there. Always launch from the same folder
to load the same client records.

### Backing up and restoring data

**To back up your data:**

1. Close the application.
2. Copy `data/addressbook.json` to a safe location outside the `data` folder.

**To restore a backup:**

1. Close the application.
2. Keep a separate copy of your current `data/addressbook.json`.
3. Replace `data/addressbook.json` with your backup.
4. Restart the application from the same working directory.

### Recovering from an invalid data file

If the data file cannot be loaded, the application starts with an empty
client list.

Do not enter commands, including `list`, `help` or `exit`: a successful
command can overwrite the existing file with the empty list.
Close the application using the window's close button instead.

Keep a copy of the unreadable file, then restore a valid backup using
the steps above.

Before manually editing the JSON file, close the application and make
a backup. Invalid formatting, invalid field values or duplicate clients
can prevent the file from loading.

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Follow Quick Start to set up the app on the new computer, then close
the app on both computers. Back up any existing data on the new computer.
Copy `data/addressbook.json` from the old computer into the `data` folder
of the new computer's working directory, creating the folder if needed.
Restart the app from that working directory.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find John`, `find 98765432`, `find johnd@example.com`
**Role** | `role INDEX ROLE`<br> e.g., `role 1 buyer`
**List** | `list`
**Help** | `help`
**Exit** | `exit`
