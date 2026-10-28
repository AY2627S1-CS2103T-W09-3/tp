package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryEntry;

/**
 * Checks the metadata and explicit recording protocol used by the persistence integration in #133.
 */
public class UndoableCommandMetadataTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_eachMutation_recordsSpecificDescriptionAndRestoresBothStates() throws Exception {
        String[][] cases = {
            {"add n/Carol New p/91234567 e/carol@example.com a/Bedok", "add", "Added client Carol New"},
            {"edit 1 n/Alicia Updated", "edit", "Edited client Alicia Updated"},
            {"delete 1", "delete", "Deleted client Alice Pauline"},
            {"clear confirm", "clear", "Cleared 2 clients"},
            {"role 1 seller", "role", "Changed role for Alice Pauline to Seller"},
            {"role 1 none", "role", "Cleared role for Alice Pauline"},
            {"note 1 Call next week", "note", "Updated note for Alice Pauline"}
        };
        for (String[] testCase : cases) {
            ModelManager model = populatedModel();
            Command command = parser.parseCommand(testCase[0]);
            AddressBookSnapshot before = model.captureSnapshot();
            command.execute(model);
            AddressBookSnapshot after = model.captureSnapshot();
            assertTrue(command.isUndoable(), testCase[0]);
            assertEquals(testCase[1], command.getCommandWord());
            assertEquals(testCase[2], command.getActionDescription());
            assertFalse(model.canUndo(), "Executing a command must not record before the save succeeds");
            assertTrue(model.recordChange(before, after, command.getCommandWord(), command.getActionDescription()));

            HistoryEntry entry = model.undo();
            assertEquals(before, model.captureSnapshot());
            assertEquals(testCase[2], entry.getActionDescription());
            model.redo();
            assertEquals(after, model.captureSnapshot());
        }
    }

    @Test
    public void execute_readOnlyCommandsAndClearWarning_areNotUndoable() throws Exception {
        for (String input : List.of("help", "list", "find Alice", "filter r/buyer", "view 1", "exit", "clear")) {
            ModelManager model = populatedModel();
            Command command = parser.parseCommand(input);
            AddressBookSnapshot before = model.captureSnapshot();
            command.execute(model);
            assertFalse(command.isUndoable(), input);
            assertEquals(before, model.captureSnapshot());
            assertFalse(model.canUndo());
            if (!input.equals("clear")) {
                assertEquals("", command.getCommandWord());
            }
            assertEquals("", command.getActionDescription());
        }
    }

    @Test
    public void execute_failedMutations_leaveDataAndHistoryUnchanged() throws Exception {
        for (String input : List.of("edit 99 n/Changed", "delete 99", "note 99 Test", "role 99 buyer",
                "add n/Alice Pauline p/91234567 e/new@example.com a/Bedok", "edit 1 n/Bob Choo")) {
            ModelManager model = populatedModel();
            Command command = parser.parseCommand(input);
            AddressBookSnapshot before = model.captureSnapshot();
            assertThrows(CommandException.class, () -> command.execute(model));
            assertEquals(before, model.captureSnapshot());
            assertFalse(model.canUndo());
            assertFalse(model.canRedo());
        }
    }

    @Test
    public void execute_noOpMutations_preserveRedoHistory() throws Exception {
        for (String input : List.of("edit 1 n/Alice Pauline", "role 1 buyer", "note 1 Existing note")) {
            ModelManager model = populatedModel();
            parser.parseCommand("role 1 buyer").execute(model);
            parser.parseCommand("note 1 Existing note").execute(model);
            AddressBookSnapshot beforeDelete = model.captureSnapshot();
            model.deletePerson(BOB);
            model.recordChange(beforeDelete, model.captureSnapshot(), "delete", "Deleted client Bob Choo");
            HistoryEntry undone = model.undo();
            Command command = parser.parseCommand(input);
            AddressBookSnapshot before = model.captureSnapshot();
            command.execute(model);
            assertFalse(model.recordChange(before, model.captureSnapshot(),
                    command.getCommandWord(), command.getActionDescription()));
            assertEquals(List.of(undone), model.getRedoHistory());
            assertFalse(model.canUndo());
        }
    }

    @Test
    public void execute_confirmedClear_countsHiddenClientsBeforeClearing() throws Exception {
        ModelManager model = populatedModel();
        model.updateFilteredPersonList(person -> person.equals(ALICE));
        ClearCommand command = new ClearCommand(true);
        AddressBookSnapshot before = model.captureSnapshot();
        command.execute(model);
        assertEquals("Cleared 2 clients", command.getActionDescription());
        assertTrue(model.recordChange(before, model.captureSnapshot(),
                command.getCommandWord(), command.getActionDescription()));
        model.undo();
        assertEquals(List.of(ALICE, BOB), model.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyClear_doesNotRecordOrDiscardRedo() throws Exception {
        ModelManager model = new ModelManager();
        AddressBookSnapshot empty = model.captureSnapshot();
        model.addPerson(ALICE);
        model.recordChange(empty, model.captureSnapshot(), "add", "Added client Alice Pauline");
        HistoryEntry entry = model.undo();
        ClearCommand command = new ClearCommand(true);
        assertEquals(ClearCommand.MESSAGE_EMPTY, command.execute(model).getFeedbackToUser());
        assertFalse(model.recordChange(empty, model.captureSnapshot(),
                command.getCommandWord(), command.getActionDescription()));
        assertEquals(List.of(entry), model.getRedoHistory());
    }

    @Test
    public void execute_noteClearAndSingleClientClear_describeRemovedData() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        parser.parseCommand("note 1 Existing note").execute(model);
        Command note = parser.parseCommand("note 1 clear");
        note.execute(model);
        assertEquals("Cleared note for Alice Pauline", note.getActionDescription());
        ClearCommand clear = new ClearCommand(true);
        clear.execute(model);
        assertEquals("Cleared 1 client", clear.getActionDescription());
        clear.execute(model);
        assertEquals("", clear.getActionDescription(), "Re-execution must not retain an earlier count");
    }

    private static ModelManager populatedModel() {
        ModelManager model = new ModelManager();
        model.addPerson(ALICE);
        model.addPerson(BOB);
        return model;
    }
}
