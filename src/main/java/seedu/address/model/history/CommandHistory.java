package seedu.address.model.history;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * Stores a bounded history of successful data-changing commands.
 *
 * <p>Entries at the front of each internal deque are the next entries to undo or redo. Every entry belongs to at most
 * one deque. Recording a new change invalidates the redo history, while undo and redo transfer one entry between the
 * deques.
 */
public class CommandHistory {

    public static final int DEFAULT_CAPACITY = 20;
    public static final String MESSAGE_NOTHING_TO_UNDO = "There is no action to undo";
    public static final String MESSAGE_NOTHING_TO_REDO = "There is no action to redo";
    public static final String MESSAGE_UNDO_NOT_REVERSIBLE = "The most recent undo cannot be reversed";
    public static final String MESSAGE_REDO_NOT_REVERSIBLE = "The most recent redo cannot be reversed";

    private final int capacity;
    private final Deque<HistoryEntry> undoHistory = new ArrayDeque<>();
    private final Deque<HistoryEntry> redoHistory = new ArrayDeque<>();

    /**
     * Creates an empty history with {@link #DEFAULT_CAPACITY}.
     */
    public CommandHistory() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates an empty history that stores at most {@code capacity} entries.
     */
    public CommandHistory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("History capacity must be positive");
        }
        this.capacity = capacity;
    }

    /**
     * Records {@code entry} as the newest undoable action.
     *
     * <p>An entry whose before and after snapshots are equal is ignored. A recorded entry invalidates every redoable
     * action. If the capacity is exceeded, the oldest undoable entry is discarded.
     *
     * @return true if the entry represented a change and was recorded.
     */
    public boolean record(HistoryEntry entry) {
        requireNonNull(entry);
        if (!entry.representsChange()) {
            return false;
        }

        undoHistory.addFirst(entry);
        redoHistory.clear();
        discardOldestEntryIfOverCapacity();
        return true;
    }

    /**
     * Takes snapshots of the supplied address books and records the resulting entry.
     *
     * @return true if the entry represented a change and was recorded.
     */
    public boolean record(ReadOnlyAddressBook before, ReadOnlyAddressBook after,
            String commandWord, String actionDescription) {
        return record(HistoryEntry.from(before, after, commandWord, actionDescription));
    }

    /**
     * Transfers and returns the newest undoable entry.
     *
     * <p>The caller can restore the entry's before snapshot. If that restoration cannot be persisted, the caller can
     * restore the history state by passing the returned entry to {@link #reverseUndo(HistoryEntry)}.
     */
    public HistoryEntry undo() throws HistoryOperationException {
        if (!canUndo()) {
            throw new HistoryOperationException(MESSAGE_NOTHING_TO_UNDO);
        }

        HistoryEntry entry = undoHistory.removeFirst();
        redoHistory.addFirst(entry);
        return entry;
    }

    /**
     * Transfers and returns the newest redoable entry.
     *
     * <p>The caller can restore the entry's after snapshot. If that restoration cannot be persisted, the caller can
     * restore the history state by passing the returned entry to {@link #reverseRedo(HistoryEntry)}.
     */
    public HistoryEntry redo() throws HistoryOperationException {
        if (!canRedo()) {
            throw new HistoryOperationException(MESSAGE_NOTHING_TO_REDO);
        }

        HistoryEntry entry = redoHistory.removeFirst();
        undoHistory.addFirst(entry);
        return entry;
    }

    /**
     * Reverses the immediately preceding call to {@link #undo()}.
     *
     * <p>The exact entry returned by that call must be supplied. Requiring object identity prevents an older, equal
     * entry from reversing the wrong transition.
     */
    public void reverseUndo(HistoryEntry undoneEntry) throws HistoryOperationException {
        requireNonNull(undoneEntry);
        if (redoHistory.peekFirst() != undoneEntry) {
            throw new HistoryOperationException(MESSAGE_UNDO_NOT_REVERSIBLE);
        }

        redoHistory.removeFirst();
        undoHistory.addFirst(undoneEntry);
    }

    /**
     * Reverses the immediately preceding call to {@link #redo()}.
     *
     * <p>The exact entry returned by that call must be supplied. Requiring object identity prevents an older, equal
     * entry from reversing the wrong transition.
     */
    public void reverseRedo(HistoryEntry redoneEntry) throws HistoryOperationException {
        requireNonNull(redoneEntry);
        if (undoHistory.peekFirst() != redoneEntry) {
            throw new HistoryOperationException(MESSAGE_REDO_NOT_REVERSIBLE);
        }

        undoHistory.removeFirst();
        redoHistory.addFirst(redoneEntry);
    }

    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUndoSize() {
        return undoHistory.size();
    }

    public int getRedoSize() {
        return redoHistory.size();
    }

    /**
     * Returns an unmodifiable snapshot of undoable entries, ordered newest first.
     */
    public List<HistoryEntry> getUndoHistory() {
        return List.copyOf(undoHistory);
    }

    /**
     * Returns an unmodifiable snapshot of redoable entries, ordered newest first.
     */
    public List<HistoryEntry> getRedoHistory() {
        return List.copyOf(redoHistory);
    }

    /**
     * Removes every undoable and redoable entry.
     */
    public void clear() {
        undoHistory.clear();
        redoHistory.clear();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("capacity", capacity)
                .add("undoHistory", undoHistory)
                .add("redoHistory", redoHistory)
                .toString();
    }

    private void discardOldestEntryIfOverCapacity() {
        if (undoHistory.size() > capacity) {
            undoHistory.removeLast();
        }
    }
}
