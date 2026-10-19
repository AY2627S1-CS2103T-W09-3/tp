package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.NoteCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validNote_updatesNoteAndPreservesOtherDetails() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(original).withNote("Call again next Monday").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, new Note("Call again next Monday"));
        String expectedMessage = String.format(NoteCommand.MESSAGE_NOTE_UPDATED, editedPerson.getName());

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_existingNote_replacesNote() {
        Person original = new PersonBuilder().withNote("Old note").build();
        Model actualModel = modelContaining(original);
        Person editedPerson = new PersonBuilder(original).withNote("Replacement note").build();
        Model expectedModel = modelContaining(editedPerson);

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, new Note("Replacement note"));
        String expectedMessage = String.format(NoteCommand.MESSAGE_NOTE_UPDATED, editedPerson.getName());

        assertCommandSuccess(command, actualModel, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clear_removesExistingNote() {
        Person original = new PersonBuilder().withNote("Old note").build();
        Model actualModel = modelContaining(original);
        Person clearedPerson = copyWithNote(original, null);
        Model expectedModel = modelContaining(clearedPerson);

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON);
        String expectedMessage = String.format(NoteCommand.MESSAGE_NOTE_CLEARED, clearedPerson.getName());

        assertCommandSuccess(command, actualModel, expectedMessage, expectedModel);
        assertNull(actualModel.getFilteredPersonList().get(0).getNote());
    }

    @Test
    public void execute_filteredList_updatesDisplayedClientAndKeepsFilter() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person original = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(original).withNote("Filtered note").build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(expectedModel.getFilteredPersonList().get(0), editedPerson);

        NoteCommand command = new NoteCommand(INDEX_FIRST_PERSON, new Note("Filtered note"));
        String expectedMessage = String.format(NoteCommand.MESSAGE_NOTE_UPDATED, editedPerson.getName());

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new NoteCommand(invalidIndex, new Note("Note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new NoteCommand(INDEX_SECOND_PERSON, new Note("Note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_twoLineMaximumLengthNote_survivesStorageRoundTrip(@TempDir Path tempDir) throws Exception {
        String encodedNote = "a".repeat(249) + "\\n" + "b".repeat(250);
        String decodedNote = "a".repeat(249) + "\n" + "b".repeat(250);
        assertEquals(Note.MAX_LENGTH, decodedNote.length());

        new NoteCommandParser().parse("1 " + encodedNote).execute(model);
        assertEquals(new Note(decodedNote), model.getFilteredPersonList().get(0).getNote());

        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("clientbook.json"));
        storage.saveAddressBook(model.getAddressBook());

        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void equals() {
        NoteCommand firstNoteCommand = new NoteCommand(INDEX_FIRST_PERSON, new Note("First note"));

        assertTrue(firstNoteCommand.equals(firstNoteCommand));
        assertTrue(firstNoteCommand.equals(new NoteCommand(INDEX_FIRST_PERSON, new Note("First note"))));
        assertTrue(new NoteCommand(INDEX_FIRST_PERSON).equals(new NoteCommand(INDEX_FIRST_PERSON)));
        assertFalse(firstNoteCommand.equals(null));
        assertFalse(firstNoteCommand.equals(new NoteCommand(INDEX_SECOND_PERSON, new Note("First note"))));
        assertFalse(firstNoteCommand.equals(new NoteCommand(INDEX_FIRST_PERSON, new Note("Second note"))));
        assertFalse(firstNoteCommand.equals(new NoteCommand(INDEX_FIRST_PERSON)));
    }

    private static Model modelContaining(Person person) {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        return new ModelManager(addressBook, new UserPrefs());
    }

    private static Person copyWithNote(Person person, Note note) {
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getRole(), note, person.getTags(), person.getBudget(), person.getAskingPrice(),
                person.getAreas(), person.getHousingTypes());
    }
}
