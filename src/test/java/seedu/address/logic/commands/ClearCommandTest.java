package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_EMPTY, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_confirmedWithFilteredList_clearsHiddenClientsToo() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_unconfirmedOneClient_warnsAndKeepsClient() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        Model model = new ModelManager(addressBook, new UserPrefs());
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        CommandResult expectedResult =
                new CommandResult(ClearCommand.MESSAGE_WARNING_ONE_CLIENT, false, false, true);

        assertCommandSuccess(new ClearCommand(false), model, expectedResult, expectedModel);
        assertEquals("This will delete the only client. Type clear confirm to proceed.",
                expectedResult.getFeedbackToUser());
    }

    @Test
    public void execute_unconfirmedMultipleClients_warnsWithCountAndKeepsClients() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        CommandResult expectedResult = new CommandResult(
                "This will delete all 7 clients. Type clear confirm to proceed.", false, false, true);

        assertCommandSuccess(new ClearCommand(false), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_unconfirmedWithFilteredList_countsWholeAddressBook() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);
        CommandResult expectedResult = new CommandResult(
                "This will delete all 7 clients. Type clear confirm to proceed.", false, false, true);

        assertCommandSuccess(new ClearCommand(false), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_unconfirmedEmptyAddressBook_reportsEmptyWithoutWarning() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();
        CommandResult expectedResult = new CommandResult(ClearCommand.MESSAGE_EMPTY);

        assertCommandSuccess(new ClearCommand(false), model, expectedResult, expectedModel);
        assertFalse(expectedResult.isAwaitingConfirmation());
    }

    @Test
    public void execute_confirmedClear_doesNotAwaitConfirmation() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        assertFalse(new ClearCommand(true).execute(model).isAwaitingConfirmation());
    }

    @Test
    public void execute_unconfirmedClear_awaitsConfirmation() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        assertTrue(new ClearCommand(false).execute(model).isAwaitingConfirmation());
    }

    @Test
    public void equals() {
        ClearCommand confirmedCommand = new ClearCommand(true);
        ClearCommand unconfirmedCommand = new ClearCommand(false);

        // same object -> returns true
        assertTrue(confirmedCommand.equals(confirmedCommand));

        // same values -> returns true
        assertTrue(confirmedCommand.equals(new ClearCommand(true)));
        assertTrue(unconfirmedCommand.equals(new ClearCommand(false)));

        // same values -> same hashcode
        assertEquals(confirmedCommand.hashCode(), new ClearCommand(true).hashCode());

        // different confirmation -> returns false
        assertFalse(confirmedCommand.equals(unconfirmedCommand));
        assertNotEquals(confirmedCommand.hashCode(), unconfirmedCommand.hashCode());

        // null -> returns false
        assertFalse(confirmedCommand.equals(null));

        // different types -> returns false
        assertFalse(confirmedCommand.equals(new ListCommand()));
    }

    @Test
    public void toStringMethod() {
        ClearCommand clearCommand = new ClearCommand(true);
        String expected = ClearCommand.class.getCanonicalName() + "{isConfirmed=true}";
        assertEquals(expected, clearCommand.toString());
    }

}
