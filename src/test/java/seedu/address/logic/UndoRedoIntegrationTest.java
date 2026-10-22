package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Exercises complete command, model, and storage flows for undo and redo. */
public class UndoRedoIntegrationTest {
    private static final String ADD_AMY = "add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road";
    private static final String ADD_BOB = "add n/Bob Choo p/81234567 e/bob@example.com a/2 Test Road";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_eachMutation_canUndoAndRedoPersistedState() throws Exception {
        String[][] mutations = {
            {"add", ADD_AMY},
            {"edit", "edit 1 p/99998888"},
            {"delete", "delete 1"},
            {"role", "role 1 seller"},
            {"note", "note 1 Call tomorrow"},
            {"clear", "clear confirm"}
        };

        for (String[] mutation : mutations) {
            String commandWord = mutation[0];
            ModelManager model = new ModelManager();
            JsonAddressBookStorage dataFile = new JsonAddressBookStorage(
                    temporaryFolder.resolve(commandWord + ".json"));
            LogicManager logic = new LogicManager(model, new StorageManager(dataFile,
                    new JsonUserPrefsStorage(temporaryFolder.resolve(commandWord + "-prefs.json"))));
            if (!commandWord.equals("add")) {
                logic.execute(ADD_AMY);
            }
            AddressBookSnapshot before = model.captureSnapshot();
            int priorHistorySize = model.getUndoHistory().size();
            if (commandWord.equals("clear")) {
                assertTrue(logic.execute("clear").isAwaitingConfirmation());
                assertEquals(priorHistorySize, model.getUndoHistory().size());
            }

            logic.execute(mutation[1]);
            AddressBookSnapshot after = model.captureSnapshot();
            assertNotEquals(before, after, commandWord);
            assertEquals(priorHistorySize + 1, model.getUndoHistory().size(), commandWord);
            assertEquals(commandWord, model.getUndoHistory().get(0).getCommandWord());
            assertEquals(after.getPersonList(), dataFile.readAddressBook().orElseThrow().getPersonList());

            logic.execute("undo");
            assertEquals(before, model.captureSnapshot(), commandWord);
            assertEquals(before.getPersonList(), dataFile.readAddressBook().orElseThrow().getPersonList());
            assertEquals(commandWord, model.getRedoHistory().get(0).getCommandWord());

            logic.execute("redo");
            assertEquals(after, model.captureSnapshot(), commandWord);
            assertEquals(after.getPersonList(), dataFile.readAddressBook().orElseThrow().getPersonList());
        }
    }

    @Test
    public void execute_multipleUndosAndRedos_thenNewChangeInvalidatesRedo() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage dataFile = new JsonAddressBookStorage(temporaryFolder.resolve("sequence.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(dataFile,
                new JsonUserPrefsStorage(temporaryFolder.resolve("sequence-prefs.json"))));
        logic.execute(ADD_AMY);
        logic.execute(ADD_BOB);
        logic.execute("note 1 Call tomorrow");
        AddressBookSnapshot afterThreeChanges = model.captureSnapshot();

        logic.execute("undo");
        logic.execute("undo");
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(3, model.getRedoHistory().size());
        assertEquals("add", model.getRedoHistory().get(0).getCommandWord());

        logic.execute("redo");
        logic.execute("redo");
        logic.execute("redo");
        assertEquals(afterThreeChanges, model.captureSnapshot());
        assertEquals(afterThreeChanges.getPersonList(), dataFile.readAddressBook().orElseThrow().getPersonList());
        assertFalse(model.canRedo());

        logic.execute("undo");
        assertTrue(model.canRedo());
        logic.execute("edit 1 p/99998888");
        assertFalse(model.canRedo());
        assertEquals("edit", model.getUndoHistory().get(0).getCommandWord());
        assertEquals(model.getAddressBook().getPersonList(),
                dataFile.readAddressBook().orElseThrow().getPersonList());
    }
}
