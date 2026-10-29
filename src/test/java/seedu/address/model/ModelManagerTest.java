package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonMatchesFilterPredicate;
import seedu.address.model.person.PersonMatchesSearchTermsPredicate;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void history_restorePreservesSearchAndRoleFiltersAndObservableLists() throws Exception {
        Person buyer = new PersonBuilder(ALICE).withRole(ClientRole.BUYER).build();
        Person changed = new PersonBuilder(buyer).withName("Zoe Changed").withEmail("zoe@example.net")
                .withRole(ClientRole.SELLER).build();
        List<Predicate<Person>> predicates = List.of(
                new PersonMatchesSearchTermsPredicate(List.of("Alice")),
                new PersonMatchesFilterPredicate.Builder().withRole(ClientRole.BUYER).build());
        for (Predicate<Person> predicate : predicates) {
            ModelManager model = new ModelManager();
            model.addPerson(buyer);
            model.addPerson(BOB);
            model.updateFilteredPersonList(predicate);
            var visibleList = model.getFilteredPersonList();
            var sourceList = model.getAddressBook().getPersonList();
            AtomicInteger notifications = new AtomicInteger();
            visibleList.addListener((ListChangeListener<Person>) change -> notifications.incrementAndGet());
            AddressBookSnapshot before = model.captureSnapshot();
            model.setPerson(buyer, changed);
            AddressBookSnapshot after = model.captureSnapshot();
            assertTrue(model.recordChange(before, after, "edit", "Edited client Alice Pauline"));
            assertTrue(visibleList.isEmpty());
            notifications.set(0);

            HistoryEntry entry = model.undo();
            assertEquals(List.of(buyer), visibleList);
            assertTrue(notifications.get() > 0);
            assertEquals(List.of(buyer, BOB), sourceList);
            model.rollbackUndo(entry);
            assertTrue(visibleList.isEmpty());
            assertEquals(after, model.captureSnapshot());
            assertEquals(List.of(entry), model.getUndoHistory());
            assertTrue(model.getRedoHistory().isEmpty());

            model.undo();
            assertSame(entry, model.redo());
            assertTrue(visibleList.isEmpty());
            model.rollbackRedo(entry);
            assertEquals(List.of(buyer), visibleList);
            assertEquals(before, model.captureSnapshot());
            assertEquals(List.of(entry), model.getRedoHistory());
            assertTrue(model.getUndoHistory().isEmpty());

            model.restoreSnapshot(after);
            assertTrue(visibleList.isEmpty());
            assertEquals(List.of(entry), model.getRedoHistory());
            assertSame(visibleList, model.getFilteredPersonList());
            assertSame(sourceList, model.getAddressBook().getPersonList());
        }
    }

    @Test
    public void history_newSessionStartsEmptyEvenWhenClientsAreLoaded() throws Exception {
        AddressBookSnapshot before = modelManager.captureSnapshot();
        modelManager.addPerson(ALICE);
        modelManager.recordChange(before, modelManager.captureSnapshot(), "add", "Added client Alice Pauline");
        assertTrue(modelManager.canUndo());
        ModelManager reopened = new ModelManager(modelManager.getAddressBook(), modelManager.getUserPrefs());
        assertEquals(List.of(ALICE), reopened.getAddressBook().getPersonList());
        assertFalse(reopened.canUndo());
        assertFalse(reopened.canRedo());
        assertThrows(HistoryOperationException.class, reopened::undo);
        assertThrows(HistoryOperationException.class, reopened::redo);
    }

    @Test
    public void history_restoreAfterUnrecordedMutation_preservesRedo() throws Exception {
        AddressBookSnapshot empty = modelManager.captureSnapshot();
        modelManager.addPerson(ALICE);
        modelManager.recordChange(empty, modelManager.captureSnapshot(), "add", "Added client Alice Pauline");
        HistoryEntry entry = modelManager.undo();
        modelManager.addPerson(BOB);

        // The save failed before recording; restore data without modifying the existing redo stack.
        modelManager.restoreSnapshot(empty);
        assertFalse(modelManager.recordChange(empty, modelManager.captureSnapshot(), "edit", "No change"));
        assertFalse(modelManager.canUndo());
        assertTrue(modelManager.canRedo());
        assertEquals(List.of(entry), modelManager.getRedoHistory());
        modelManager.redo();
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void addPerson_activeFilter_keepsFilterActive() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BOB);
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(List.of("Alice"));
        modelManager.updateFilteredPersonList(predicate);

        modelManager.addPerson(new PersonBuilder().withName("Carl Young").withPhone("81112222")
                .withEmail("carlyoung@example.com").build());

        // The new client does not match the filter, so it is added but hidden.
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertEquals(3, modelManager.getAddressBook().getPersonList().size());
    }

    @Test
    public void addPerson_activeFilter_showsNewPersonThatMatches() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BOB);
        PersonMatchesSearchTermsPredicate predicate = new PersonMatchesSearchTermsPredicate(List.of("Alice"));
        modelManager.updateFilteredPersonList(predicate);

        Person matchingPerson = new PersonBuilder().withName("Alice Young").withPhone("81112222")
                .withEmail("aliceyoung@example.com").build();
        modelManager.addPerson(matchingPerson);

        assertEquals(List.of(ALICE, matchingPerson), modelManager.getFilteredPersonList());
    }

    @Test
    public void findPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.findPerson(null));
    }

    @Test
    public void findPerson_noPersonMatches_returnsEmpty() {
        modelManager.addPerson(BOB);
        assertEquals(Optional.empty(), modelManager.findPerson(ALICE));
    }

    @Test
    public void findPerson_duplicateInAddressBook_returnsExistingPerson() {
        modelManager.addPerson(ALICE);
        Person editedBob = new PersonBuilder(BOB).withPhone(ALICE.getPhone().value).build();
        assertEquals(Optional.of(ALICE), modelManager.findPerson(editedBob));
    }

    @Test
    public void findPersonOtherThan_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.findPersonOtherThan(null, ALICE));
        assertThrows(NullPointerException.class, () -> modelManager.findPersonOtherThan(ALICE, null));
    }

    @Test
    public void findPersonOtherThan_onlyExcludedPersonMatches_returnsEmpty() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BOB);
        assertEquals(Optional.empty(), modelManager.findPersonOtherThan(BOB, BOB));
    }

    @Test
    public void findPersonOtherThan_otherPersonMatches_returnsOtherPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BOB);
        Person editedBob = new PersonBuilder(BOB).withPhone(ALICE.getPhone().value).build();
        assertEquals(Optional.of(ALICE), modelManager.findPersonOtherThan(BOB, editedBob));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new PersonMatchesSearchTermsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
