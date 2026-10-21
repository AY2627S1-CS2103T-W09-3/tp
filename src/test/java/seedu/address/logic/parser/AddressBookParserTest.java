package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.RoleCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonMatchesFilterPredicate;
import seedu.address.model.person.PersonMatchesSearchTermsPredicate;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertEquals(new ClearCommand(false), parser.parseCommand(ClearCommand.COMMAND_WORD));
        assertEquals(new ClearCommand(true),
                parser.parseCommand(ClearCommand.COMMAND_WORD + " " + ClearCommand.CONFIRM_WORD));
        assertThrows(ParseException.class, ClearCommand.MESSAGE_INVALID_FORMAT, ()
                -> parser.parseCommand(ClearCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new PersonMatchesSearchTermsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_filter() throws Exception {
        FilterCommand command = (FilterCommand) parser.parseCommand(
                FilterCommand.COMMAND_WORD + " r/buyer t/urgent");
        PersonMatchesFilterPredicate predicate =
                new PersonMatchesFilterPredicate(Optional.of(ClientRole.BUYER), Set.of(new Tag("urgent")));
        assertEquals(new FilterCommand(predicate), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_historyCommands_acceptOnlyWhitespaceArguments() throws Exception {
        assertTrue(parser.parseCommand("undo") instanceof UndoCommand);
        assertTrue(parser.parseCommand("redo  ") instanceof RedoCommand);
        assertTrue(parser.parseCommand("history\t") instanceof HistoryCommand);
        assertThrows(ParseException.class, UndoCommand.MESSAGE_INVALID_FORMAT, () ->
                parser.parseCommand("undo 1"));
        assertThrows(ParseException.class, RedoCommand.MESSAGE_INVALID_FORMAT, () ->
                parser.parseCommand("redo now"));
        assertThrows(ParseException.class, HistoryCommand.MESSAGE_INVALID_FORMAT, () ->
                parser.parseCommand("history extra"));
    }

    @Test
    public void parseCommand_note() throws Exception {
        NoteCommand command = (NoteCommand) parser.parseCommand(
                NoteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " Call next week");
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Note("Call next week")), command);
    }

    @Test
    public void parseCommand_role() throws Exception {
        RoleCommand command = (RoleCommand) parser.parseCommand(
                RoleCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " buyer");
        assertEquals(new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BUYER), command);
    }

    @Test
    public void parseCommand_view() throws Exception {
        ViewCommand command = (ViewCommand) parser.parseCommand("view 2");

        assertEquals(new ViewCommand(INDEX_SECOND_PERSON), command);
    }

    @Test
    public void parseCommand_viewAfterFind_returnsDisplayedClient() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parseCommand("find Meier").execute(model);

        CommandResult result = parser.parseCommand("view 2").execute(model);

        assertEquals("Showing Daniel Meier.", result.getFeedbackToUser());
        assertEquals(Optional.of(INDEX_SECOND_PERSON), result.getViewIndex());
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void parseCommand_viewAfterFilter_returnsDisplayedClient() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parseCommand("filter r/buyer").execute(model);

        CommandResult result = parser.parseCommand("view 2").execute(model);

        assertEquals("Showing Carl Kurz.", result.getFeedbackToUser());
        assertEquals(Optional.of(INDEX_SECOND_PERSON), result.getViewIndex());
        assertEquals(List.of(ALICE, CARL), model.getFilteredPersonList());
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void parseCommand_viewWithExtraArguments_throwsParseException() {
        assertThrows(ParseException.class, ViewCommand.MESSAGE_INVALID_FORMAT, () -> {
            parser.parseCommand("view 1 extra");
        });
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
