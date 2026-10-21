package seedu.address.model.history;

import static java.util.Objects.requireNonNull;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * An immutable snapshot of the contents of an address book.
 *
 * <p>The person list is defensively copied when the snapshot is created. Persons are safe to share because
 * {@link Person} is immutable. The list exposed through {@link #getPersonList()} is unmodifiable and is never used as
 * mutable storage by a restored address book.
 */
public final class AddressBookSnapshot implements ReadOnlyAddressBook {

    private final ObservableList<Person> persons;

    /**
     * Creates a snapshot containing the current state of {@code source}.
     */
    public AddressBookSnapshot(ReadOnlyAddressBook source) {
        requireNonNull(source);
        persons = FXCollections.unmodifiableObservableList(
                FXCollections.observableArrayList(source.getPersonList()));
    }

    /**
     * Returns a snapshot containing the current state of {@code source}.
     */
    public static AddressBookSnapshot copyOf(ReadOnlyAddressBook source) {
        return new AddressBookSnapshot(source);
    }

    /**
     * Returns the number of persons stored in this snapshot.
     */
    public int size() {
        return persons.size();
    }

    /**
     * Returns true if this snapshot contains no persons.
     */
    public boolean isEmpty() {
        return persons.isEmpty();
    }

    /**
     * Restores this snapshot into a new, independently mutable address book.
     */
    public AddressBook restore() {
        return new AddressBook(this);
    }

    /**
     * Replaces the contents of {@code target} with this snapshot.
     */
    public void restoreInto(AddressBook target) {
        requireNonNull(target);
        target.resetData(this);
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBookSnapshot otherSnapshot)) {
            return false;
        }

        return persons.equals(otherSnapshot.persons);
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }
}
