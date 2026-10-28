package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.CommandHistory;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.person.Person;

public class HistoryCommandsTest {
    private final ModelManager model = new ModelManager();

    @Test
    public void execute_emptyHistory_reportsUnavailableActions() {
        assertThrows(CommandException.class, CommandHistory.MESSAGE_NOTHING_TO_UNDO, () ->
                new UndoCommand().execute(model));
        assertThrows(CommandException.class, CommandHistory.MESSAGE_NOTHING_TO_REDO, () ->
                new RedoCommand().execute(model));
        CommandResult historyResult = new HistoryCommand().execute(model);
        assertEquals(HistoryCommand.MESSAGE_EMPTY_HISTORY, historyResult.getFeedbackToUser());
        assertEquals(CommandResult.preservingSelection(HistoryCommand.MESSAGE_EMPTY_HISTORY), historyResult);
    }

    @Test
    public void execute_undoRedo_reportsActionAndKeepsExactEntry() throws Exception {
        HistoryEntry alice = recordAdd(ALICE);
        HistoryEntry bob = recordAdd(BOB);

        UndoCommand undo = new UndoCommand();
        assertEquals("Undid: Added client Bob Choo", undo.execute(model).getFeedbackToUser());
        assertSame(bob, undo.getUndoneEntry());
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        String expectedHistory = "Undo history:\n1. Added client Alice Pauline"
                + "\nRedo history:\n1. Added client Bob Choo";
        assertEquals(CommandResult.preservingSelection(expectedHistory), new HistoryCommand().execute(model));

        RedoCommand redo = new RedoCommand();
        assertEquals("Redid: Added client Bob Choo", redo.execute(model).getFeedbackToUser());
        assertSame(bob, redo.getRedoneEntry());
        assertEquals(List.of(ALICE, BOB), model.getAddressBook().getPersonList());
        assertEquals(List.of(bob, alice), model.getUndoHistory());
    }

    @Test
    public void execute_historyListsNewestFirstWithinEachStack() throws Exception {
        recordAdd(ALICE);
        recordAdd(BOB);

        assertEquals("Undo history:\n1. Added client Bob Choo\n2. Added client Alice Pauline"
                        + "\nRedo history:\nNone",
                new HistoryCommand().execute(model).getFeedbackToUser());
    }

    private HistoryEntry recordAdd(Person person) {
        AddressBookSnapshot before = model.captureSnapshot();
        model.addPerson(person);
        model.recordChange(before, model.captureSnapshot(), "add", "Added client " + person.getName());
        return model.getUndoHistory().get(0);
    }
}
