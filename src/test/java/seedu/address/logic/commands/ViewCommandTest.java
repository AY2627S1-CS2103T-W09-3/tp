package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.KEYWORD_MATCHING_MEIER;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.PersonMatchesSearchTermsPredicate;

/**
 * Tests viewing clients using a real model, including filtered indexes and unchanged client data.
 */
public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        ViewCommand viewCommand = new ViewCommand(INDEX_SECOND_PERSON);
        CommandResult expectedResult = new CommandResult("Showing Benson Meier.", INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(viewCommand, model, expectedResult, expectedModel);
    }

    @Test
    public void execute_lastIndexUnfilteredList_success() {
        Index lastIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        CommandResult expectedResult = new CommandResult("Showing George Best.", lastIndex);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ViewCommand(lastIndex), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        PersonMatchesSearchTermsPredicate predicate =
                new PersonMatchesSearchTermsPredicate(List.of(KEYWORD_MATCHING_MEIER));
        model.updateFilteredPersonList(predicate);
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());

        CommandResult expectedResult = new CommandResult("Showing Daniel Meier.", INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(new ViewCommand(INDEX_SECOND_PERSON), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ViewCommand(outOfBoundIndex), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        model.updateFilteredPersonList(new PersonMatchesSearchTermsPredicate(List.of(KEYWORD_MATCHING_MEIER)));
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new ViewCommand(outOfBoundIndex), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), new ModelManager(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_noSearchResults_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);

        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void equals() {
        ViewCommand viewFirstCommand = new ViewCommand(INDEX_FIRST_PERSON);

        assertTrue(viewFirstCommand.equals(viewFirstCommand));
        assertTrue(viewFirstCommand.equals(new ViewCommand(Index.fromOneBased(1))));
        assertFalse(viewFirstCommand.equals(new ViewCommand(INDEX_SECOND_PERSON)));
        assertFalse(viewFirstCommand.equals(new DeleteCommand(INDEX_FIRST_PERSON)));
        assertFalse(viewFirstCommand.equals(null));
    }

    @Test
    public void hashCode_equalIndexes_sameHashCode() {
        ViewCommand firstCommand = new ViewCommand(Index.fromOneBased(1));
        ViewCommand secondCommand = new ViewCommand(Index.fromOneBased(1));

        assertEquals(firstCommand, secondCommand);
        assertEquals(firstCommand.hashCode(), secondCommand.hashCode());
    }

    @Test
    public void toStringMethod() {
        ViewCommand command = new ViewCommand(INDEX_FIRST_PERSON);
        String expected = ViewCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}";

        assertEquals(expected, command.toString());
    }
}
