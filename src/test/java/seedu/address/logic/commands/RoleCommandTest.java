package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class RoleCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_updatesRoleAndPreservesOtherDetails() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedClient = new PersonBuilder(original).withRole(ClientRole.BUYER).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedClient);

        RoleCommand command = new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BUYER);
        String expectedMessage = String.format(RoleCommand.MESSAGE_SUCCESS,
                editedClient.getName(), ClientRole.BUYER.getDisplayValue());

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_updatesDisplayedClient() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedClient = new PersonBuilder(original).withRole(ClientRole.BOTH).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedClient);

        RoleCommand command = new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BOTH);
        String expectedMessage = String.format(RoleCommand.MESSAGE_SUCCESS,
                editedClient.getName(), ClientRole.BOTH.getDisplayValue());

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index invalidIndex = Index.fromZeroBased(model.getFilteredPersonList().size());
        RoleCommand command = new RoleCommand(invalidIndex, ClientRole.SELLER);

        assertCommandFailure(command, model, RoleCommand.MESSAGE_INVALID_CLIENT_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        RoleCommand command = new RoleCommand(INDEX_SECOND_PERSON, ClientRole.SELLER);

        assertCommandFailure(command, model, RoleCommand.MESSAGE_INVALID_CLIENT_INDEX);
    }

    @Test
    public void execute_updatedRole_survivesStorageRoundTrip(@TempDir Path tempDir) throws Exception {
        new RoleCommand(INDEX_FIRST_PERSON, ClientRole.SELLER).execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("clientbook.json"));

        storage.saveAddressBook(model.getAddressBook());

        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void equals() {
        RoleCommand firstBuyerCommand = new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BUYER);

        assertTrue(firstBuyerCommand.equals(firstBuyerCommand));
        assertTrue(firstBuyerCommand.equals(new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BUYER)));
        assertFalse(firstBuyerCommand.equals(null));
        assertFalse(firstBuyerCommand.equals(new RoleCommand(INDEX_SECOND_PERSON, ClientRole.BUYER)));
        assertFalse(firstBuyerCommand.equals(new RoleCommand(INDEX_FIRST_PERSON, ClientRole.SELLER)));
    }
}
