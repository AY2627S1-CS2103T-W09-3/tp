package seedu.address.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);


    /** Returns an immutable snapshot of all clients, including clients hidden by the current filter. */
    AddressBookSnapshot captureSnapshot();

    /** Restores client data in place, preserving the current filter and both history stacks. */
    void restoreSnapshot(AddressBookSnapshot snapshot);

    /**
     * Records a successfully saved mutation. The caller must not pass failed, cancelled or read-only commands.
     * Returns false for equal snapshots without invalidating redo history. Never saves to storage.
     */
    boolean recordChange(AddressBookSnapshot before, AddressBookSnapshot after,
            String commandWord, String actionDescription);

    /** Returns whether a recorded change can be undone. */
    boolean canUndo();

    /** Returns whether an undone change can be redone. */
    boolean canRedo();

    /**
     * Restores the previous state and moves its entry to redo history, preserving the active filter.
     * @throws HistoryOperationException if there is nothing to undo.
     */
    HistoryEntry undo() throws HistoryOperationException;

    /**
     * Restores the next state and moves its entry to undo history, preserving the active filter.
     * @throws HistoryOperationException if there is nothing to redo.
     */
    HistoryEntry redo() throws HistoryOperationException;

    /**
     * Reverses the latest undo's data and stack changes after saving fails. Supply the exact returned entry
     * before any other successful history operation or explicit snapshot restoration.
     * @throws HistoryOperationException if the entry does not identify the latest undo transition.
     */
    void rollbackUndo(HistoryEntry entry) throws HistoryOperationException;

    /**
     * Reverses the latest redo's data and stack changes after saving fails. Supply the exact returned entry
     * before any other successful history operation or explicit snapshot restoration.
     * @throws HistoryOperationException if the entry does not identify the latest redo transition.
     */
    void rollbackRedo(HistoryEntry entry) throws HistoryOperationException;

    /** Returns an immutable snapshot of undo history, with the next entry to undo first. */
    List<HistoryEntry> getUndoHistory();

    /** Returns an immutable snapshot of redo history, with the next entry to redo first. */
    List<HistoryEntry> getRedoHistory();

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Returns the first person in the address book that is a duplicate of {@code candidate}, if any.
     */
    Optional<Person> findPerson(Person candidate);

    /**
     * Returns the first person in the address book, other than {@code excludedPerson}, that is a duplicate of
     * {@code candidate}, if any.
     */
    Optional<Person> findPersonOtherThan(Person excludedPerson, Person candidate);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
