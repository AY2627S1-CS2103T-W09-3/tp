package seedu.address.model.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import seedu.address.model.AddressBook;
import seedu.address.testutil.AddressBookBuilder;

public class CommandHistoryTest {

    private static final String ADD_COMMAND_WORD = "add";
    private static final String DELETE_COMMAND_WORD = "delete";
    private static final String ADD_ALICE_DESCRIPTION = "Added Alice Pauline";
    private static final String ADD_BOB_DESCRIPTION = "Added Bob Choo";
    private static final String ADD_CARL_DESCRIPTION = "Added Carl Kurz";

    private final AddressBook emptyAddressBook = new AddressBook();
    private final AddressBook addressBookWithAlice = new AddressBookBuilder().withPerson(ALICE).build();
    private final AddressBook addressBookWithAliceAndBob = new AddressBookBuilder()
            .withPerson(ALICE).withPerson(BOB).build();
    private final AddressBook addressBookWithAliceBobAndCarl = new AddressBookBuilder()
            .withPerson(ALICE).withPerson(BOB).withPerson(CARL).build();

    private final HistoryEntry addAliceEntry = HistoryEntry.from(emptyAddressBook, addressBookWithAlice,
            ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION);
    private final HistoryEntry addBobEntry = HistoryEntry.from(addressBookWithAlice, addressBookWithAliceAndBob,
            ADD_COMMAND_WORD, ADD_BOB_DESCRIPTION);
    private final HistoryEntry addCarlEntry = HistoryEntry.from(addressBookWithAliceAndBob,
            addressBookWithAliceBobAndCarl, ADD_COMMAND_WORD, ADD_CARL_DESCRIPTION);

    @Test
    public void constructor_nonPositiveCapacity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CommandHistory(0));
        assertThrows(IllegalArgumentException.class, () -> new CommandHistory(-1));
    }

    @Test
    public void constructor_validCapacity_createsEmptyHistory() {
        CommandHistory history = new CommandHistory(3);

        assertEquals(3, history.getCapacity());
        assertEquals(0, history.getUndoSize());
        assertEquals(0, history.getRedoSize());
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    public void defaultConstructor_usesDefaultCapacity() {
        CommandHistory history = new CommandHistory();

        assertEquals(CommandHistory.DEFAULT_CAPACITY, history.getCapacity());
    }

    @Test
    public void record_nullEntry_throwsNullPointerException() {
        CommandHistory history = new CommandHistory();

        assertThrows(NullPointerException.class, () -> history.record((HistoryEntry) null));
    }

    @Test
    public void record_changedEntry_addsEntryToUndoHistory() {
        CommandHistory history = new CommandHistory();

        assertTrue(history.record(addAliceEntry));

        assertEquals(List.of(addAliceEntry), history.getUndoHistory());
        assertEquals(1, history.getUndoSize());
        assertTrue(history.canUndo());
    }

    @Test
    public void record_readOnlyAddressBooks_takesDefensiveSnapshots() {
        CommandHistory history = new CommandHistory();

        history.record(emptyAddressBook, addressBookWithAlice, ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION);
        addressBookWithAlice.addPerson(BOB);

        assertEquals(List.of(ALICE), history.getUndoHistory().get(0)
                .getAfterSnapshot().getPersonList());
    }

    @Test
    public void record_unchangedEntry_ignoresEntry() {
        CommandHistory history = new CommandHistory();
        HistoryEntry unchangedEntry = HistoryEntry.from(addressBookWithAlice, addressBookWithAlice,
                DELETE_COMMAND_WORD, "No client deleted");

        assertFalse(history.record(unchangedEntry));

        assertFalse(history.canUndo());
        assertTrue(history.getUndoHistory().isEmpty());
    }

    @Test
    public void record_afterUndo_clearsRedoHistory() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.undo();

        history.record(addCarlEntry);

        assertFalse(history.canRedo());
        assertTrue(history.getRedoHistory().isEmpty());
        assertEquals(List.of(addCarlEntry, addAliceEntry), history.getUndoHistory());
    }

    @Test
    public void record_unchangedEntryAfterUndo_preservesRedoHistory() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.undo();
        HistoryEntry unchangedEntry = HistoryEntry.from(addressBookWithAlice, addressBookWithAlice,
                DELETE_COMMAND_WORD, "No client deleted");

        history.record(unchangedEntry);

        assertEquals(List.of(addAliceEntry), history.getRedoHistory());
    }

    @Test
    public void record_overCapacity_discardsOldestUndoEntry() {
        CommandHistory history = new CommandHistory(2);

        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.record(addCarlEntry);

        assertEquals(List.of(addCarlEntry, addBobEntry), history.getUndoHistory());
        assertEquals(2, history.getUndoSize());
    }

    @Test
    public void getHistoryViews_modifyViews_throwsUnsupportedOperationException() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.undo();
        List<HistoryEntry> undoView = history.getUndoHistory();
        List<HistoryEntry> redoView = history.getRedoHistory();

        assertThrows(UnsupportedOperationException.class, () -> undoView.add(addCarlEntry));
        assertThrows(UnsupportedOperationException.class, redoView::clear);
        assertEquals(List.of(addAliceEntry), history.getUndoHistory());
        assertEquals(List.of(addBobEntry), history.getRedoHistory());
    }

    @Test
    public void getHistoryViews_historyLaterModified_returnedViewsDoNotChange() {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        List<HistoryEntry> earlierView = history.getUndoHistory();

        history.record(addBobEntry);

        assertEquals(List.of(addAliceEntry), earlierView);
        assertEquals(List.of(addBobEntry, addAliceEntry), history.getUndoHistory());
    }

    @Test
    public void undo_emptyHistory_throwsHistoryOperationException() {
        CommandHistory history = new CommandHistory();

        assertThrows(HistoryOperationException.class, CommandHistory.MESSAGE_NOTHING_TO_UNDO, history::undo);
    }

    @Test
    public void undo_populatedHistory_returnsNewestEntryAndMakesItRedoable() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);

        HistoryEntry undoneEntry = history.undo();

        assertSame(addBobEntry, undoneEntry);
        assertEquals(List.of(addAliceEntry), history.getUndoHistory());
        assertEquals(List.of(addBobEntry), history.getRedoHistory());
    }

    @Test
    public void undo_multipleEntries_ordersRedoHistoryNewestFirst() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.record(addCarlEntry);

        history.undo();
        history.undo();

        assertEquals(List.of(addBobEntry, addCarlEntry), history.getRedoHistory());
    }

    @Test
    public void redo_emptyHistory_throwsHistoryOperationException() {
        CommandHistory history = new CommandHistory();

        assertThrows(HistoryOperationException.class, CommandHistory.MESSAGE_NOTHING_TO_REDO, history::redo);
    }

    @Test
    public void redo_afterUndo_returnsEntryAndMakesItUndoable() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.undo();

        HistoryEntry redoneEntry = history.redo();

        assertSame(addBobEntry, redoneEntry);
        assertEquals(List.of(addBobEntry, addAliceEntry), history.getUndoHistory());
        assertTrue(history.getRedoHistory().isEmpty());
    }

    @Test
    public void reverseUndo_nullEntry_throwsNullPointerException() {
        CommandHistory history = new CommandHistory();

        assertThrows(NullPointerException.class, () -> history.reverseUndo(null));
    }

    @Test
    public void reverseUndo_withoutMatchingTransition_throwsHistoryOperationException()
            throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.undo();
        Executable reverseWrongEntry = () -> history.reverseUndo(addAliceEntry);

        assertThrows(HistoryOperationException.class, CommandHistory.MESSAGE_UNDO_NOT_REVERSIBLE,
                reverseWrongEntry);
    }

    @Test
    public void reverseUndo_equalButDifferentEntry_throwsHistoryOperationException()
            throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.undo();
        HistoryEntry equalEntry = HistoryEntry.from(emptyAddressBook, addressBookWithAlice,
                ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION);
        Executable reverseEqualEntry = () -> history.reverseUndo(equalEntry);

        assertEquals(addAliceEntry, equalEntry);
        assertThrows(HistoryOperationException.class, CommandHistory.MESSAGE_UNDO_NOT_REVERSIBLE,
                reverseEqualEntry);
    }

    @Test
    public void reverseUndo_afterSimulatedSaveFailure_restoresHistoryState() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        List<HistoryEntry> originalUndoHistory = history.getUndoHistory();
        HistoryEntry undoneEntry = history.undo();

        try {
            simulateSaveFailure();
        } catch (IOException exception) {
            history.reverseUndo(undoneEntry);
        }

        assertEquals(originalUndoHistory, history.getUndoHistory());
        assertTrue(history.getRedoHistory().isEmpty());
    }

    @Test
    public void reverseRedo_nullEntry_throwsNullPointerException() {
        CommandHistory history = new CommandHistory();

        assertThrows(NullPointerException.class, () -> history.reverseRedo(null));
    }

    @Test
    public void reverseRedo_withoutMatchingTransition_throwsHistoryOperationException()
            throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.undo();
        history.redo();
        Executable reverseWrongEntry = () -> history.reverseRedo(addAliceEntry);

        assertThrows(HistoryOperationException.class, CommandHistory.MESSAGE_REDO_NOT_REVERSIBLE,
                reverseWrongEntry);
    }

    @Test
    public void reverseRedo_afterSimulatedSaveFailure_restoresHistoryState() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.undo();
        List<HistoryEntry> originalUndoHistory = history.getUndoHistory();
        List<HistoryEntry> originalRedoHistory = history.getRedoHistory();
        HistoryEntry redoneEntry = history.redo();

        try {
            simulateSaveFailure();
        } catch (IOException exception) {
            history.reverseRedo(redoneEntry);
        }

        assertEquals(originalUndoHistory, history.getUndoHistory());
        assertEquals(originalRedoHistory, history.getRedoHistory());
    }

    @Test
    public void clear_populatedUndoAndRedoHistories_clearsBoth() throws HistoryOperationException {
        CommandHistory history = new CommandHistory();
        history.record(addAliceEntry);
        history.record(addBobEntry);
        history.undo();

        history.clear();

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(0, history.getUndoSize());
        assertEquals(0, history.getRedoSize());
    }

    @Test
    public void historyEntry_nullArguments_throwsNullPointerException() {
        AddressBookSnapshot before = AddressBookSnapshot.copyOf(emptyAddressBook);
        AddressBookSnapshot after = AddressBookSnapshot.copyOf(addressBookWithAlice);

        assertThrows(NullPointerException.class, () ->
                new HistoryEntry(null, after, ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION));
        assertThrows(NullPointerException.class, () ->
                new HistoryEntry(before, null, ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION));
        assertThrows(NullPointerException.class, () ->
                new HistoryEntry(before, after, null, ADD_ALICE_DESCRIPTION));
        assertThrows(NullPointerException.class, () ->
                new HistoryEntry(before, after, ADD_COMMAND_WORD, null));
    }

    @Test
    public void historyEntry_blankText_throwsIllegalArgumentException() {
        AddressBookSnapshot before = AddressBookSnapshot.copyOf(emptyAddressBook);
        AddressBookSnapshot after = AddressBookSnapshot.copyOf(addressBookWithAlice);

        assertThrows(IllegalArgumentException.class, () ->
                new HistoryEntry(before, after, " ", ADD_ALICE_DESCRIPTION));
        assertThrows(IllegalArgumentException.class, () ->
                new HistoryEntry(before, after, ADD_COMMAND_WORD, "\t"));
    }

    @Test
    public void historyEntry_gettersReturnConstructorValues() {
        AddressBookSnapshot before = AddressBookSnapshot.copyOf(emptyAddressBook);
        AddressBookSnapshot after = AddressBookSnapshot.copyOf(addressBookWithAlice);
        HistoryEntry entry = new HistoryEntry(before, after, ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION);

        assertSame(before, entry.getBeforeSnapshot());
        assertSame(after, entry.getAfterSnapshot());
        assertEquals(ADD_COMMAND_WORD, entry.getCommandWord());
        assertEquals(ADD_ALICE_DESCRIPTION, entry.getActionDescription());
        assertTrue(entry.representsChange());
    }

    @Test
    public void historyEntry_equals() {
        HistoryEntry equalEntry = HistoryEntry.from(emptyAddressBook, addressBookWithAlice,
                ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION);
        HistoryEntry differentAfterEntry = HistoryEntry.from(emptyAddressBook, addressBookWithAliceAndBob,
                ADD_COMMAND_WORD, ADD_ALICE_DESCRIPTION);
        HistoryEntry differentCommandEntry = HistoryEntry.from(emptyAddressBook, addressBookWithAlice,
                DELETE_COMMAND_WORD, ADD_ALICE_DESCRIPTION);
        HistoryEntry differentDescriptionEntry = HistoryEntry.from(emptyAddressBook, addressBookWithAlice,
                ADD_COMMAND_WORD, "Created Alice");

        assertEquals(addAliceEntry, addAliceEntry);
        assertEquals(addAliceEntry, equalEntry);
        assertEquals(equalEntry, addAliceEntry);
        assertEquals(addAliceEntry.hashCode(), equalEntry.hashCode());
        assertNotEquals(addAliceEntry, addBobEntry);
        assertNotEquals(addAliceEntry, differentAfterEntry);
        assertNotEquals(addAliceEntry, differentCommandEntry);
        assertNotEquals(addAliceEntry, differentDescriptionEntry);
        assertNotEquals(addAliceEntry, null);
        assertNotEquals(addAliceEntry, "add");
    }

    @Test
    public void toStringMethods_includeHistoryStateAndEntryDetails() {
        CommandHistory history = new CommandHistory(2);
        history.record(addAliceEntry);

        String expectedEntry = HistoryEntry.class.getCanonicalName()
                + "{beforeSnapshot=" + addAliceEntry.getBeforeSnapshot()
                + ", afterSnapshot=" + addAliceEntry.getAfterSnapshot()
                + ", commandWord=" + ADD_COMMAND_WORD
                + ", actionDescription=" + ADD_ALICE_DESCRIPTION + "}";
        String expectedHistory = CommandHistory.class.getCanonicalName()
                + "{capacity=2, undoHistory=[" + expectedEntry + "], redoHistory=[]}";

        assertEquals(expectedEntry, addAliceEntry.toString());
        assertEquals(expectedHistory, history.toString());
    }

    private void simulateSaveFailure() throws IOException {
        throw new IOException("Simulated save failure");
    }
}
