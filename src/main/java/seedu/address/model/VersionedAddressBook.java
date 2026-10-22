package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.CommandHistory;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;

/**
 * Coordinates an existing address book and its session-only history. Restoring data updates the same observable
 * list, so filters and UI listeners remain attached. Persistence and the decision to record belong to the caller.
 */
public class VersionedAddressBook {
    private final AddressBook addressBook;
    private final CommandHistory history = new CommandHistory();
    private HistoryEntry rollbackEntry;
    private boolean lastOperationWasUndo;

    /** Creates empty history for the supplied address book without replacing its client list. */
    public VersionedAddressBook(AddressBook addressBook) {
        this.addressBook = requireNonNull(addressBook);
    }

    /** Returns an immutable copy of all clients. */
    public AddressBookSnapshot captureSnapshot() {
        return AddressBookSnapshot.copyOf(addressBook);
    }

    /** Restores data only, leaving both stacks unchanged and invalidating any earlier rollback token. */
    public void restoreSnapshot(AddressBookSnapshot snapshot) {
        requireNonNull(snapshot).restoreInto(addressBook);
        rollbackEntry = null;
    }

    /** Records a saved change, ignoring equal snapshots without clearing redo history. */
    public boolean recordChange(AddressBookSnapshot before, AddressBookSnapshot after,
            String commandWord, String actionDescription) {
        requireNonNull(before);
        requireNonNull(after);
        if (before.equals(after)) {
            return false;
        }
        boolean recorded = history.record(new HistoryEntry(before, after, commandWord, actionDescription));
        rollbackEntry = null;
        return recorded;
    }

    public boolean canUndo() {
        return history.canUndo();
    }

    public boolean canRedo() {
        return history.canRedo();
    }

    /** Restores the before state and returns the exact entry needed to roll back a failed undo save. */
    public HistoryEntry undo() throws HistoryOperationException {
        HistoryEntry entry = history.undo();
        entry.getBeforeSnapshot().restoreInto(addressBook);
        rollbackEntry = entry;
        lastOperationWasUndo = true;
        return entry;
    }

    /** Restores the after state and returns the exact entry needed to roll back a failed redo save. */
    public HistoryEntry redo() throws HistoryOperationException {
        HistoryEntry entry = history.redo();
        entry.getAfterSnapshot().restoreInto(addressBook);
        rollbackEntry = entry;
        lastOperationWasUndo = false;
        return entry;
    }

    /** Reverses the latest undo's data and stack changes; invalid requests change neither. */
    public void rollbackUndo(HistoryEntry entry) throws HistoryOperationException {
        requireNonNull(entry);
        if (entry != rollbackEntry || !lastOperationWasUndo) {
            throw new HistoryOperationException(CommandHistory.MESSAGE_UNDO_NOT_REVERSIBLE);
        }
        history.reverseUndo(entry);
        entry.getAfterSnapshot().restoreInto(addressBook);
        rollbackEntry = null;
    }

    /** Reverses the latest redo's data and stack changes; invalid requests change neither. */
    public void rollbackRedo(HistoryEntry entry) throws HistoryOperationException {
        requireNonNull(entry);
        if (entry != rollbackEntry || lastOperationWasUndo) {
            throw new HistoryOperationException(CommandHistory.MESSAGE_REDO_NOT_REVERSIBLE);
        }
        history.reverseRedo(entry);
        entry.getBeforeSnapshot().restoreInto(addressBook);
        rollbackEntry = null;
    }

    /** Returns an immutable snapshot, with the next action to undo first. */
    public List<HistoryEntry> getUndoHistory() {
        return history.getUndoHistory();
    }

    /** Returns an immutable snapshot, with the next action to redo first. */
    public List<HistoryEntry> getRedoHistory() {
        return history.getRedoHistory();
    }
}
