package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RoleCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryEntry;
import seedu.address.model.history.HistoryOperationException;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Person;
import seedu.address.model.person.Price;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_cancelledClear_preservesDataAndExistingHistory() throws Exception {
        AddressBookSnapshot empty = model.captureSnapshot();
        model.addPerson(AMY);
        model.recordChange(empty, model.captureSnapshot(), "add", "Added client Amy Bee");
        AddressBookSnapshot beforeBob = model.captureSnapshot();
        model.addPerson(BOB);
        model.recordChange(beforeBob, model.captureSnapshot(), "add", "Added client Bob Choo");
        model.undo();
        List<HistoryEntry> undo = model.getUndoHistory();
        List<HistoryEntry> redo = model.getRedoHistory();

        assertTrue(logic.execute("clear").isAwaitingConfirmation());
        assertEquals(ClearCommand.MESSAGE_CANCELLED, logic.execute("delete 1").getFeedbackToUser());

        assertEquals(beforeBob, model.captureSnapshot());
        assertEquals(undo, model.getUndoHistory());
        assertEquals(redo, model.getRedoHistory());
    }

    @Test
    public void execute_addNameWithExtraSpaces_isRejectedAsDuplicate() throws Exception {
        logic.execute("add n/Alpha Tester p/90010001 e/alpha.tester@example.com a/1 Test Road");

        assertCommandException("add n/Alpha  Tester p/90010301 e/alpha.spaces@example.com a/21 Test Road",
                Messages.MESSAGE_DUPLICATE_NAME);
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addEmailInDifferentCase_isRejectedAsDuplicate() throws Exception {
        logic.execute("add n/Alpha Tester p/90010001 e/alpha.tester@example.com a/1 Test Road");

        assertCommandException("add n/Email Case Test p/90010302 e/alpha.tester@EXAMPLE.COM a/22 Test Road",
                String.format(Messages.MESSAGE_DUPLICATE_EMAIL, "Alpha Tester"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addNameWithExtraSpaces_isStoredWithSingleSpaces() throws Exception {
        logic.execute("add n/Alpha   Tester p/90010001 e/alpha.tester@example.com a/1 Test Road");

        assertEquals("Alpha Tester", model.getAddressBook().getPersonList().get(0).getName().fullName);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_missingRole_reportsCommandFormat() {
        assertParseException("role", RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseException("role 1", RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseException("role 1 buyer extra", RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        assertParseException("role 1 agent", RoleCommand.MESSAGE_INVALID_ROLE);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_mutation_recordsHistoryOnlyAfterSave() throws Exception {
        Path filePath = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(filePath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                assertFalse(model.canUndo(), "The change must not be recorded before saving");
                super.saveAddressBook(addressBook);
            }
        };
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        logic.execute("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road");

        assertTrue(model.canUndo());
        assertEquals("Added client Amy Bee", model.getUndoHistory().get(0).getActionDescription());
        assertEquals(model.getAddressBook().getPersonList(),
                addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_readOnlyAndNoOpCommands_doNotRecordHistory() throws Exception {
        logic.execute("list");
        logic.execute("clear");
        assertFalse(model.canUndo());

        logic.execute("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road");
        List<HistoryEntry> recorded = model.getUndoHistory();
        logic.execute("edit 1 n/Amy Bee");
        logic.execute("list");

        assertEquals(recorded, model.getUndoHistory());
    }

    @Test
    public void execute_addWithAllRequirements_savesAndReloadsAllFields() throws Exception {
        String addCommand = "add n/John Doe p/98765432 e/john@example.com a/311 Clementi Ave 2 "
                + "b/100000-200000 s/150000 l/Bukit Timah l/Orchard h/Condo h/HDB";
        logic.execute(addCommand);

        Person expectedPerson = new PersonBuilder()
                .withName("John Doe")
                .withPhone("98765432")
                .withEmail("john@example.com")
                .withAddress("311 Clementi Ave 2")
                .withBudget(new Budget(new Price("100000"), new Price("200000")))
                .withAskingPrice(new Price("150000"))
                .withAreas(Set.of(new Area("Bukit Timah"), new Area("Orchard")))
                .withHousingTypes(Set.of(HousingType.CONDO, HousingType.HDB))
                .build();

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        ReadOnlyAddressBook reloadedAddressBook = storage.readAddressBook().orElseThrow();
        assertEquals(List.of(expectedPerson), reloadedAddressBook.getPersonList());
    }

    @Test
    public void execute_readOnlyCommandsWithUnwritableStorage_succeed() throws Exception {
        model.addPerson(AMY);
        logic = new LogicManager(model, createStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
        assertEquals(Messages.getShowingMessage(1, 1), logic.execute("find Amy").getFeedbackToUser());
        assertEquals(String.format(ViewCommand.MESSAGE_SUCCESS, AMY.getName()),
                logic.execute("view 1").getFeedbackToUser());
        assertTrue(logic.execute("clear").isAwaitingConfirmation());
    }

    @Test
    public void execute_readOnlyCommand_doesNotSave() throws Exception {
        model.addPerson(AMY);
        CountingAddressBookStorage storage = createCountingStorage();

        logic.execute("list");
        logic.execute("find Amy");
        logic.execute("view 1");
        logic.execute("clear");

        assertEquals(0, storage.getSaveCount());
    }

    @Test
    public void execute_modifyingCommand_savesOnce() throws Exception {
        CountingAddressBookStorage storage = createCountingStorage();

        logic.execute("add" + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY);

        assertEquals(1, storage.getSaveCount());
    }

    @Test
    public void execute_changeThatSnapshotsCannotSee_stillSaves() throws Exception {
        // A model whose snapshots never differ stands in for a field that model equality ignores.
        AddressBookSnapshot blindSnapshot = new ModelManager().captureSnapshot();
        model = new ModelManager() {
            @Override
            public AddressBookSnapshot captureSnapshot() {
                return blindSnapshot;
            }
        };
        CountingAddressBookStorage storage = createCountingStorage();

        logic.execute("add" + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY);

        assertEquals(1, storage.getSaveCount());
        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());
    }

    @Test
    public void execute_noteTextThatLooksLikeNull_stillSaves() throws Exception {
        // A client with no note and a client whose note text is "null" look the same to Person#toString.
        model.addPerson(AMY);
        CountingAddressBookStorage storage = createCountingStorage();

        logic.execute("note 1 null");

        assertEquals(1, storage.getSaveCount());
        assertEquals("null",
                storage.readAddressBook().orElseThrow().getPersonList().get(0).getNote().toString());
    }

    @Test
    public void execute_tagEditThatChangesOnlyCase_stillSaves() throws Exception {
        // Person equality compares tag names, so urgent and Urgent differ; the file must record the new casing.
        model.addPerson(new PersonBuilder(AMY).withTags("urgent").build());
        CountingAddressBookStorage storage = createCountingStorage();

        logic.execute("edit 1 t/Urgent");

        assertEquals(1, storage.getSaveCount());
        Person saved = storage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertEquals("Urgent", saved.getTags().iterator().next().tagName);
    }

    @Test
    public void execute_readOnlyCommandsWithTaggedClients_doNotSave() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withTags("urgent", "friend").build());
        CountingAddressBookStorage storage = createCountingStorage();

        logic.execute("list");
        logic.execute("view 1");
        logic.execute("find Amy");
        logic.execute("history");

        assertEquals(0, storage.getSaveCount());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_saveFails_restoresDataAndBothHistoryStacks() throws Exception {
        AddressBookSnapshot empty = model.captureSnapshot();
        model.addPerson(AMY);
        model.recordChange(empty, model.captureSnapshot(), "add", "Added client Amy Bee");
        HistoryEntry undone = model.undo();
        List<HistoryEntry> undo = model.getUndoHistory();
        List<HistoryEntry> redo = model.getRedoHistory();
        logic = new LogicManager(model, createStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandException("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road",
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));

        assertEquals(empty, model.captureSnapshot());
        assertEquals(undo, model.getUndoHistory());
        assertEquals(undone, redo.get(0));
        assertEquals(redo, model.getRedoHistory());
    }

    @Test
    public void execute_undoRedo_savesEachRestoredAddressBook() throws Exception {
        JsonAddressBookStorage dataFile = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        logic.execute("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road");
        AddressBookSnapshot afterAdd = model.captureSnapshot();

        assertEquals("Undid: Added client Amy Bee", logic.execute("undo").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(dataFile.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertEquals("Undo history:\nNone\nRedo history:\n1. Added client Amy Bee",
                logic.execute("history").getFeedbackToUser());

        assertEquals("Redid: Added client Amy Bee", logic.execute("redo").getFeedbackToUser());
        assertEquals(afterAdd, model.captureSnapshot());
        assertEquals(afterAdd.getPersonList(), dataFile.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_emptyHistory_reportsUnavailableActions() throws Exception {
        assertCommandException("undo", "There is no action to undo");
        assertCommandException("redo", "There is no action to redo");
        assertEquals(HistoryCommand.MESSAGE_EMPTY_HISTORY, logic.execute("history").getFeedbackToUser());
    }

    @Test
    public void execute_undoSaveFails_restoresDataAndHistoryStacks() throws Exception {
        JsonAddressBookStorage dataFile = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        logic.execute("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road");
        AddressBookSnapshot afterAdd = model.captureSnapshot();
        List<HistoryEntry> undo = model.getUndoHistory();
        logic = new LogicManager(model, createStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandException("undo", String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));

        assertEquals(afterAdd, model.captureSnapshot());
        assertEquals(undo, model.getUndoHistory());
        assertFalse(model.canRedo());
        assertEquals(afterAdd.getPersonList(), dataFile.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_redoSaveFails_restoresDataAndHistoryStacks() throws Exception {
        JsonAddressBookStorage dataFile = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        logic.execute("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road");
        logic.execute("undo");
        AddressBookSnapshot afterUndo = model.captureSnapshot();
        List<HistoryEntry> redo = model.getRedoHistory();
        logic = new LogicManager(model, createStorageThatFailsToSave(DUMMY_AD_EXCEPTION));

        assertCommandException("redo", String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));

        assertEquals(afterUndo, model.captureSnapshot());
        assertEquals(redo, model.getRedoHistory());
        assertFalse(model.canUndo());
        assertTrue(dataFile.readAddressBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void execute_undoSaveAndRollbackFail_reportsHistoryFailure() throws Exception {
        model = new ModelManager() {
            @Override
            public void rollbackUndo(HistoryEntry entry) throws HistoryOperationException {
                throw new HistoryOperationException("History changed before rollback");
            }
        };
        JsonAddressBookStorage dataFile = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        logic = new LogicManager(model, new StorageManager(dataFile,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
        logic.execute("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road");
        logic = new LogicManager(model, createStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertThrows(IllegalStateException.class, "Could not restore history after a failed save", () ->
                logic.execute("undo"));
    }

    @Test
    public void execute_noOpAfterUndo_keepsRedoUntilNextChange() throws Exception {
        logic.execute("add n/Amy Bee p/87654321 e/amy@example.com a/1 Test Road");
        logic.execute("add n/Bob Choo p/81234567 e/bob@example.com a/2 Test Road");
        logic.execute("undo");
        List<HistoryEntry> redo = model.getRedoHistory();

        logic.execute("edit 1 n/Amy Bee");
        assertEquals(redo, model.getRedoHistory());

        logic.execute("add n/Carol New p/91234567 e/carol@example.com a/3 Test Road");
        assertFalse(model.canRedo());
    }

    @Test
    public void execute_filterThenAdd_keepsFilterAndHidesClientThatDoesNotMatch() throws Exception {
        Person[] clients = addFilterTestClients();
        logic.execute("filter t/urgent");
        assertEquals(List.of(clients[0], clients[1]), model.getFilteredPersonList());

        logic.execute("add n/Dan Ho p/84444444 e/dan@example.com a/1 Test Road t/weekend");
        assertEquals(List.of(clients[0], clients[1]), model.getFilteredPersonList());
        assertEquals(4, model.getAddressBook().getPersonList().size());

        logic.execute("add n/Eve Ong p/85555555 e/eve@example.com a/2 Test Road t/urgent");
        assertEquals(3, model.getFilteredPersonList().size());
        assertEquals("Eve Ong", model.getFilteredPersonList().get(2).getName().fullName);
    }

    @Test
    public void execute_filterThenDelete_keepsFilterAndUsesFilteredIndex() throws Exception {
        Person[] clients = addFilterTestClients();
        logic.execute("filter r/buyer");
        assertEquals(List.of(clients[0], clients[2]), model.getFilteredPersonList());

        // Index 2 is the second client shown, not the second client in the address book.
        logic.execute("delete 2");

        assertEquals(List.of(clients[0]), model.getFilteredPersonList());
        assertEquals(List.of(clients[0], clients[1]), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_filterThenRole_keepsFilterAndClientLeavesWhenNoLongerMatching() throws Exception {
        Person[] clients = addFilterTestClients();
        logic.execute("filter r/buyer");

        logic.execute("role 1 seller");

        assertEquals(List.of(clients[2]), model.getFilteredPersonList());
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_roleNone_savesClearedRoleAndSupportsUndoRedo() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withRole(ClientRole.BUYER).build());

        logic.execute("role 1 none");

        assertNull(model.getAddressBook().getPersonList().get(0).getRole());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertNull(storage.readAddressBook().orElseThrow().getPersonList().get(0).getRole());

        logic.execute("undo");
        assertEquals(ClientRole.BUYER, model.getAddressBook().getPersonList().get(0).getRole());

        logic.execute("redo");
        assertNull(model.getAddressBook().getPersonList().get(0).getRole());
    }

    @Test
    public void execute_roleNoneWhenAlreadyUnassigned_preservesRedoHistory() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withRole(null).build());
        logic.execute("role 1 buyer");
        logic.execute("undo");

        CommandResult result = logic.execute("role 1 none");

        assertEquals("Role unchanged: Amy Bee already has no assigned role", result.getFeedbackToUser());
        assertNull(model.getAddressBook().getPersonList().get(0).getRole());
        assertTrue(model.canRedo());
        logic.execute("redo");
        assertEquals(ClientRole.BUYER, model.getAddressBook().getPersonList().get(0).getRole());
    }

    @Test
    public void execute_roleNoneWithInvalidIndex_doesNotChangeClient() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withRole(ClientRole.BUYER).build());
        AddressBookSnapshot before = model.captureSnapshot();
        String expectedMessage = RoleCommand.MESSAGE_INVALID_CLIENT_INDEX;

        assertCommandException("role 999 none", expectedMessage);

        assertEquals(before, model.captureSnapshot());
        assertFalse(model.canUndo());
    }

    @Test
    public void execute_filterThenEdit_keepsFilterAndClientLeavesWhenNoLongerMatching() throws Exception {
        Person[] clients = addFilterTestClients();
        logic.execute("filter t/urgent");

        logic.execute("edit 1 t/weekend");

        assertEquals(List.of(clients[1]), model.getFilteredPersonList());
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_filterThenMutations_stayFilteredUntilList() throws Exception {
        addFilterTestClients();
        logic.execute("filter r/buyer");

        logic.execute("role 1 both");
        logic.execute("edit 1 p/86666666");

        assertEquals(2, model.getFilteredPersonList().size());

        logic.execute("list");
        assertEquals(3, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_clearThenConfirm_warnsThenClears() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);

        CommandResult warning = logic.execute("clear");
        assertEquals("This will delete all 2 clients. Type clear confirm to proceed.", warning.getFeedbackToUser());
        assertTrue(warning.isAwaitingConfirmation());
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertFalse(model.canUndo());

        CommandResult cleared = logic.execute("clear confirm");
        assertEquals(ClearCommand.MESSAGE_SUCCESS, cleared.getFeedbackToUser());
        assertFalse(cleared.isAwaitingConfirmation());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals("Cleared 2 clients", model.getUndoHistory().get(0).getActionDescription());
    }

    @Test
    public void execute_clearThenConfirmInCapitals_clears() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        logic.execute("clear");

        CommandResult cleared = logic.execute("clear CONFIRM");

        assertEquals(ClearCommand.MESSAGE_SUCCESS, cleared.getFeedbackToUser());
        assertFalse(cleared.isAwaitingConfirmation());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals("Cleared 2 clients", model.getUndoHistory().get(0).getActionDescription());
    }

    @Test
    public void execute_clearThenOtherCommand_cancelsWithoutExecutingIt() throws Exception {
        model.addPerson(AMY);
        logic.execute("clear");

        CommandResult cancelled = logic.execute("delete 1");

        assertEquals(ClearCommand.MESSAGE_CANCELLED, cancelled.getFeedbackToUser());
        assertFalse(cancelled.isAwaitingConfirmation());
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertCommandException("clear confirm", ClearCommand.MESSAGE_NO_CLEAR_PENDING);
    }

    @Test
    public void execute_clearThenUnparseableInput_cancelsWithoutError() throws Exception {
        model.addPerson(AMY);
        logic.execute("clear");

        CommandResult cancelled = logic.execute("uicfhmowqewca");

        assertEquals(ClearCommand.MESSAGE_CANCELLED, cancelled.getFeedbackToUser());
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertCommandException("clear confirm", ClearCommand.MESSAGE_NO_CLEAR_PENDING);
    }

    @Test
    public void execute_clearThenClear_cancelsThenWarnsAgain() throws Exception {
        model.addPerson(AMY);
        logic.execute("clear");

        assertEquals(ClearCommand.MESSAGE_CANCELLED, logic.execute("clear").getFeedbackToUser());

        CommandResult warning = logic.execute("clear");
        assertEquals(ClearCommand.MESSAGE_WARNING_ONE_CLIENT, warning.getFeedbackToUser());
        assertTrue(warning.isAwaitingConfirmation());
    }

    @Test
    public void execute_clearConfirmWithoutPendingClear_throwsCommandException() {
        model.addPerson(AMY);

        assertCommandException("clear confirm", ClearCommand.MESSAGE_NO_CLEAR_PENDING);
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_clearConfirmInCapitalsWithoutPendingClear_throwsCommandException() {
        model.addPerson(AMY);

        assertCommandException("clear CONFIRM", ClearCommand.MESSAGE_NO_CLEAR_PENDING);
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_clearOnEmptyAddressBook_doesNotStartPendingClear() throws Exception {
        CommandResult result = logic.execute("clear");

        assertEquals(ClearCommand.MESSAGE_EMPTY, result.getFeedbackToUser());
        assertFalse(result.isAwaitingConfirmation());
        assertCommandException("clear confirm", ClearCommand.MESSAGE_NO_CLEAR_PENDING);
    }

    @Test
    public void execute_clearWarningWithUnwritableStorage_warnsThenClearsOnlyOnConfirm() throws Exception {
        model.addPerson(AMY);
        logic = new LogicManager(model, createStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        // The warning does not write, so an unwritable location still shows it.
        assertTrue(logic.execute("clear").isAwaitingConfirmation());

        // Confirming does write, so the failure surfaces there and the data is restored.
        assertCommandException("clear confirm", String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
        assertCommandException("clear confirm", ClearCommand.MESSAGE_NO_CLEAR_PENDING);
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Adds an urgent buyer, an urgent seller and a buyer tagged weekend to the model, in that order,
     * and returns them.
     */
    private Person[] addFilterTestClients() {
        Person[] clients = {
            new PersonBuilder().withName("Alice Tan").withPhone("81111111").withEmail("alice@example.com")
                    .withRole(ClientRole.BUYER).withTags("urgent").build(),
            new PersonBuilder().withName("Ben Lim").withPhone("82222222").withEmail("ben@example.com")
                    .withRole(ClientRole.SELLER).withTags("urgent").build(),
            new PersonBuilder().withName("Cara Goh").withPhone("83333333").withEmail("cara@example.com")
                    .withRole(ClientRole.BUYER).withTags("weekend").build()
        };
        for (Person client : clients) {
            model.addPerson(client);
        }
        return clients;
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        logic = new LogicManager(model, createStorageThatFailsToSave(e));

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, new ModelManager());
        assertFalse(model.canUndo());
    }

    /**
     * Returns a {@code StorageManager} whose address book storage throws {@code e} when saving.
     */
    private StorageManager createStorageThatFailsToSave(IOException e) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        return new StorageManager(addressBookStorage, userPrefsStorage);
    }

    /**
     * Points {@code logic} at a storage that counts saves, and returns it.
     */
    private CountingAddressBookStorage createCountingStorage() {
        CountingAddressBookStorage addressBookStorage =
                new CountingAddressBookStorage(temporaryFolder.resolve("countingAddressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("countingUserPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
        return addressBookStorage;
    }

    /** An address book storage that records how many times saving was attempted. */
    private static class CountingAddressBookStorage extends JsonAddressBookStorage {
        private int saveCount;

        CountingAddressBookStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveCount++;
            super.saveAddressBook(addressBook);
        }

        int getSaveCount() {
            return saveCount;
        }
    }
}
