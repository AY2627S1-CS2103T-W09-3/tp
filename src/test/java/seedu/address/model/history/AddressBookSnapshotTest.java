package seedu.address.model.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.testutil.AddressBookBuilder;

public class AddressBookSnapshotTest {

    private final AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddressBookSnapshot(null));
    }

    @Test
    public void constructor_sourceLaterModified_snapshotDoesNotChange() {
        AddressBookSnapshot snapshot = new AddressBookSnapshot(addressBook);

        addressBook.addPerson(BOB);

        assertEquals(List.of(ALICE), snapshot.getPersonList());
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        AddressBookSnapshot snapshot = new AddressBookSnapshot(addressBook);

        assertThrows(UnsupportedOperationException.class, () -> snapshot.getPersonList().add(BOB));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getPersonList().remove(ALICE));
        assertEquals(List.of(ALICE), snapshot.getPersonList());
    }

    @Test
    public void sizeAndIsEmpty_reflectSnapshotContents() {
        AddressBookSnapshot emptySnapshot = new AddressBookSnapshot(new AddressBook());
        AddressBookSnapshot populatedSnapshot = new AddressBookSnapshot(addressBook);

        assertEquals(0, emptySnapshot.size());
        assertTrue(emptySnapshot.isEmpty());
        assertEquals(1, populatedSnapshot.size());
        assertFalse(populatedSnapshot.isEmpty());
    }

    @Test
    public void restore_returnsEqualIndependentAddressBook() {
        AddressBookSnapshot snapshot = new AddressBookSnapshot(addressBook);

        AddressBook restoredAddressBook = snapshot.restore();
        restoredAddressBook.addPerson(BOB);

        assertEquals(List.of(ALICE), snapshot.getPersonList());
        assertEquals(List.of(ALICE, BOB), restoredAddressBook.getPersonList());
    }

    @Test
    public void restoreInto_nullTarget_throwsNullPointerException() {
        AddressBookSnapshot snapshot = new AddressBookSnapshot(addressBook);

        assertThrows(NullPointerException.class, () -> snapshot.restoreInto(null));
    }

    @Test
    public void restoreInto_replacesTargetContentsWithoutLinkingState() {
        AddressBookSnapshot snapshot = new AddressBookSnapshot(addressBook);
        AddressBook target = new AddressBookBuilder().withPerson(BOB).build();

        snapshot.restoreInto(target);
        assertEquals(List.of(ALICE), target.getPersonList());

        target.addPerson(BOB);
        assertEquals(List.of(ALICE), snapshot.getPersonList());
    }

    @Test
    public void equals() {
        AddressBookSnapshot snapshot = new AddressBookSnapshot(addressBook);
        AddressBookSnapshot equalSnapshot = AddressBookSnapshot.copyOf(addressBook);
        AddressBookSnapshot differentSnapshot = new AddressBookSnapshot(
                new AddressBookBuilder().withPerson(BOB).build());

        assertEquals(snapshot, snapshot);
        assertEquals(snapshot, equalSnapshot);
        assertEquals(equalSnapshot, snapshot);
        assertEquals(snapshot.hashCode(), equalSnapshot.hashCode());
        assertNotEquals(snapshot, differentSnapshot);
        assertNotEquals(snapshot, null);
        assertNotEquals(snapshot, addressBook);
    }

    @Test
    public void toStringMethod() {
        AddressBookSnapshot snapshot = new AddressBookSnapshot(addressBook);
        String expected = AddressBookSnapshot.class.getCanonicalName()
                + "{persons=" + snapshot.getPersonList() + "}";

        assertEquals(expected, snapshot.toString());
    }
}
