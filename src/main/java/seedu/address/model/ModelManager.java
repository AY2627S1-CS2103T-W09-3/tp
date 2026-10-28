package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final VersionedAddressBook versionedAddressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final BooleanBinding personListFiltered;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        versionedAddressBook = new VersionedAddressBook(this.addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList(), PREDICATE_SHOW_ALL_PERSONS);
        personListFiltered = Bindings.createBooleanBinding(() ->
                filteredPersons.getPredicate() != PREDICATE_SHOW_ALL_PERSONS,
                filteredPersons.predicateProperty());
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public AddressBookSnapshot captureSnapshot() {
        return versionedAddressBook.captureSnapshot();
    }

    @Override
    public void restoreSnapshot(AddressBookSnapshot snapshot) {
        versionedAddressBook.restoreSnapshot(snapshot);
    }

    @Override
    public boolean recordChange(AddressBookSnapshot before, AddressBookSnapshot after,
            String commandWord, String actionDescription) {
        return versionedAddressBook.recordChange(before, after, commandWord, actionDescription);
    }

    @Override
    public boolean canUndo() {
        return versionedAddressBook.canUndo();
    }

    @Override
    public boolean canRedo() {
        return versionedAddressBook.canRedo();
    }

    @Override
    public HistoryEntry undo() throws HistoryOperationException {
        return versionedAddressBook.undo();
    }

    @Override
    public HistoryEntry redo() throws HistoryOperationException {
        return versionedAddressBook.redo();
    }

    @Override
    public void rollbackUndo(HistoryEntry entry) throws HistoryOperationException {
        versionedAddressBook.rollbackUndo(entry);
    }

    @Override
    public void rollbackRedo(HistoryEntry entry) throws HistoryOperationException {
        versionedAddressBook.rollbackRedo(entry);
    }

    @Override
    public List<HistoryEntry> getUndoHistory() {
        return versionedAddressBook.getUndoHistory();
    }

    @Override
    public List<HistoryEntry> getRedoHistory() {
        return versionedAddressBook.getRedoHistory();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public Optional<Person> findPerson(Person candidate) {
        requireNonNull(candidate);
        return addressBook.findPerson(candidate);
    }

    @Override
    public Optional<Person> findPersonOtherThan(Person excludedPerson, Person candidate) {
        requireAllNonNull(excludedPerson, candidate);
        return addressBook.findPersonOtherThan(excludedPerson, candidate);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public ObservableValue<Boolean> personListFilteredProperty() {
        return personListFiltered;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }

}
