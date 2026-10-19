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
import seedu.address.model.person.Person;
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
