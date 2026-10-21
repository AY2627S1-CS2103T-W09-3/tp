package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.history.AddressBookSnapshot;
import seedu.address.model.history.HistoryEntry;
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
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
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

        CommandResult cleared = logic.execute("clear confirm");
        assertEquals(ClearCommand.MESSAGE_SUCCESS, cleared.getFeedbackToUser());
        assertFalse(cleared.isAwaitingConfirmation());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
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
    public void execute_clearOnEmptyAddressBook_doesNotStartPendingClear() throws Exception {
        CommandResult result = logic.execute("clear");

        assertEquals(ClearCommand.MESSAGE_EMPTY, result.getFeedbackToUser());
        assertFalse(result.isAwaitingConfirmation());
        assertCommandException("clear confirm", ClearCommand.MESSAGE_NO_CLEAR_PENDING);
    }

    @Test
    public void execute_storageFailsAfterClearWarning_doesNotStartPendingClear() throws Exception {
        model.addPerson(AMY);
        logic = new LogicManager(model, createStorageThatFailsToSave(DUMMY_IO_EXCEPTION));

        assertCommandException("clear", String.format(
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
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
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
}
