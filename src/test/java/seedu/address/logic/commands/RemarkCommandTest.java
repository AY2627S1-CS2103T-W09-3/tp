package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_addReplaceClearRemark_preservesOtherFields() throws Exception {
        Person original = model.getFilteredPersonList().getFirst();
        CommandResult result = parser.parseCommand("remark 1 r/Likes swimming").execute(model);
        assertEquals(new PersonBuilder(original).withRemark("Likes swimming").build(),
                model.getFilteredPersonList().getFirst());
        assertEquals(String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(original)),
                result.getFeedbackToUser());
        parser.parseCommand("remark 1 r/New note").execute(model);
        assertEquals("New note", model.getFilteredPersonList().getFirst().getRemark().value);
        result = parser.parseCommand("remark 1 r/").execute(model);
        assertEquals(original, model.getFilteredPersonList().getFirst());
        assertEquals(String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(original)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() throws Exception {
        Person first = model.getFilteredPersonList().getFirst();
        Person second = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        parser.parseCommand("remark 1 r/Selected person").execute(model);
        assertEquals(first, model.getFilteredPersonList().getFirst());
        assertEquals(new PersonBuilder(second).withRemark("Selected person").build(),
                model.getFilteredPersonList().get(1));
    }

    @Test
    public void execute_invalidDisplayedIndex_throwsWithoutChangingModel() {
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        RemarkCommand command = new RemarkCommand(Index.fromOneBased(100), new Remark("note"));
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, exception.getMessage());
        assertEquals(expected, model);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertThrows(CommandException.class, () ->
                new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note")).execute(model));
    }

    @Test
    public void execute_editAfterRemark_keepsRemark() throws Exception {
        parser.parseCommand("remark 1 r/Keep this note").execute(model);
        parser.parseCommand("edit 1 p/91234567").execute(model);
        assertEquals("Keep this note", model.getFilteredPersonList().getFirst().getRemark().value);
        assertEquals("91234567", model.getFilteredPersonList().getFirst().getPhone().value);
        parser.parseCommand("remark 1").execute(model);
        assertEquals("", model.getFilteredPersonList().getFirst().getRemark().value);
    }

    @Test
    public void equals_comparesIndexAndRemark() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        assertEquals(command, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note")));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("other"))));
        assertFalse(command.equals(null));
    }
}
