package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;
import seedu.address.model.person.Person;

public class VersionedAddressBookTest {
    private final AddressBook book = new AddressBook();
    private final VersionedAddressBook versioned = new VersionedAddressBook(book);

    @Test
    public void constructor_nullBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new VersionedAddressBook(null));
    }

    @Test
    public void undoRedo_multipleChanges_restoreDataAndOrder() throws Exception {
        assertFalse(versioned.canUndo());
        assertFalse(versioned.canRedo());
        assertThrows(HistoryOperationException.class, versioned::undo);
        assertThrows(HistoryOperationException.class, versioned::redo);
        recordAdd(ALICE);
        recordAdd(BOB);
        HistoryEntry bob = versioned.undo();
        HistoryEntry alice = versioned.undo();
        assertTrue(book.getPersonList().isEmpty());
        assertEquals(List.of(alice, bob), versioned.getRedoHistory());
        assertSame(alice, versioned.redo());
        assertEquals(List.of(ALICE), book.getPersonList());
        assertSame(bob, versioned.redo());
        assertEquals(List.of(ALICE, BOB), book.getPersonList());
        assertEquals(List.of(bob, alice), versioned.getUndoHistory());
    }

    @Test
    public void restoreSnapshot_changesDataWithoutChangingStacksOrListIdentity() throws Exception {
        recordAdd(ALICE);
        AddressBookSnapshot before = versioned.captureSnapshot();
        recordAdd(BOB);
        versioned.undo();
        List<HistoryEntry> undo = versioned.getUndoHistory();
        List<HistoryEntry> redo = versioned.getRedoHistory();
        var originalList = book.getPersonList();
        book.addPerson(CARL);
        assertEquals(List.of(ALICE), before.getPersonList());

        versioned.restoreSnapshot(before);

        assertSame(originalList, book.getPersonList());
        assertEquals(List.of(ALICE), book.getPersonList());
        assertEquals(undo, versioned.getUndoHistory());
        assertEquals(redo, versioned.getRedoHistory());
        assertThrows(NullPointerException.class, () -> versioned.restoreSnapshot(null));
    }

    @Test
    public void recordChange_noOpPreservesRedo_realChangeInvalidatesIt() throws Exception {
        recordAdd(ALICE);
        recordAdd(BOB);
        versioned.undo();
        List<HistoryEntry> redo = versioned.getRedoHistory();
        AddressBookSnapshot unchanged = versioned.captureSnapshot();

        assertFalse(versioned.recordChange(unchanged, unchanged, "edit", "Unchanged client"));
        assertEquals(redo, versioned.getRedoHistory());
        recordAdd(CARL);
        assertFalse(versioned.canRedo());
        assertEquals(2, versioned.getUndoHistory().size());
    }

    @Test
    public void rollbackUndoAndRedo_restoreDataAndBothStacks() throws Exception {
        recordAdd(ALICE);
        recordAdd(BOB);
        List<HistoryEntry> originalUndo = versioned.getUndoHistory();
        HistoryEntry entry = versioned.undo();
        versioned.rollbackUndo(entry);
        assertEquals(List.of(ALICE, BOB), book.getPersonList());
        assertEquals(originalUndo, versioned.getUndoHistory());
        assertTrue(versioned.getRedoHistory().isEmpty());
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackUndo(entry));

        versioned.undo();
        List<HistoryEntry> undo = versioned.getUndoHistory();
        List<HistoryEntry> redo = versioned.getRedoHistory();
        assertSame(entry, versioned.redo());
        versioned.rollbackRedo(entry);
        assertEquals(List.of(ALICE), book.getPersonList());
        assertEquals(undo, versioned.getUndoHistory());
        assertEquals(redo, versioned.getRedoHistory());
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackRedo(entry));
    }

    @Test
    public void rollback_wrongDirectionOrEqualCopy_changesNothing() throws Exception {
        recordAdd(ALICE);
        HistoryEntry entry = versioned.undo();
        HistoryEntry copy = new HistoryEntry(entry.getBeforeSnapshot(), entry.getAfterSnapshot(),
                entry.getCommandWord(), entry.getActionDescription());
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackUndo(copy));
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackRedo(entry));
        assertEquals(List.of(entry), versioned.getRedoHistory());
        assertTrue(versioned.getUndoHistory().isEmpty());
        assertTrue(book.getPersonList().isEmpty());

        versioned.redo();
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackRedo(copy));
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackUndo(entry));
        assertEquals(List.of(entry), versioned.getUndoHistory());
        assertTrue(versioned.getRedoHistory().isEmpty());
        assertEquals(List.of(ALICE), book.getPersonList());
        assertThrows(NullPointerException.class, () -> versioned.rollbackUndo(null));
        assertThrows(NullPointerException.class, () -> versioned.rollbackRedo(null));
    }

    @Test
    public void rollback_afterRecordOrRestore_rejectsStaleEntry() throws Exception {
        recordAdd(ALICE);
        HistoryEntry entry = versioned.undo();
        recordAdd(BOB);
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackUndo(entry));
        assertEquals(List.of(BOB), book.getPersonList());

        HistoryEntry undone = versioned.undo();
        versioned.restoreSnapshot(versioned.captureSnapshot());
        assertThrows(HistoryOperationException.class, () -> versioned.rollbackUndo(undone));
        assertTrue(book.getPersonList().isEmpty());
        assertEquals(List.of(undone), versioned.getRedoHistory());
    }

    @Test
    public void recordChange_invalidArguments_doesNotDiscardRedo() throws Exception {
        AddressBookSnapshot empty = versioned.captureSnapshot();
        recordAdd(ALICE);
        AddressBookSnapshot populated = versioned.captureSnapshot();
        HistoryEntry entry = versioned.undo();
        assertThrows(NullPointerException.class, () -> versioned.recordChange(null, populated, "add", "Added Alice"));
        assertThrows(NullPointerException.class, () -> versioned.recordChange(empty, null, "add", "Added Alice"));
        assertThrows(IllegalArgumentException.class, () -> versioned.recordChange(empty, populated, "add", ""));
        assertEquals(List.of(entry), versioned.getRedoHistory());
        assertTrue(versioned.getUndoHistory().isEmpty());
        assertTrue(book.getPersonList().isEmpty());
    }

    @Test
    public void historyViews_areImmutableSnapshots() throws Exception {
        recordAdd(ALICE);
        List<HistoryEntry> undo = versioned.getUndoHistory();
        assertThrows(UnsupportedOperationException.class, undo::clear);
        versioned.undo();
        List<HistoryEntry> redo = versioned.getRedoHistory();
        assertThrows(UnsupportedOperationException.class, redo::clear);
        versioned.redo();
        assertEquals(1, undo.size());
        assertEquals(1, redo.size());
    }

    private void recordAdd(Person person) {
        AddressBookSnapshot before = versioned.captureSnapshot();
        book.addPerson(person);
        assertTrue(versioned.recordChange(before, versioned.captureSnapshot(), "add",
                "Added client " + person.getName()));
    }
}
