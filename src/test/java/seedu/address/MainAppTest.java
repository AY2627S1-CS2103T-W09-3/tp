package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.SampleAddressBookLoader;
import seedu.address.testutil.TypicalPersons;
import seedu.address.ui.Ui;

public class MainAppTest {

    private static final Path V1_4_SPACED_NAMES_FILE = Paths.get("src", "test", "data",
            "JsonSerializableAddressBookTest", "duplicateNameSpacesAddressBook.json");
    private static final String ADD_CLIENT_COMMAND =
            "add n/Recovered Client p/91234567 e/recovered@example.com a/1 Recovered Road";

    @TempDir
    public Path temporaryFolder;

    private MainApp app;
    private Path userPrefsPath;
    private Path addressBookPath;
    private Logger logger;
    private final LogHandler logHandler = new LogHandler();

    @BeforeEach
    public void setUp() {
        userPrefsPath = temporaryFolder.resolve("preferences.json");
        addressBookPath = temporaryFolder.resolve("addressbook.json");
        app = new MainApp(userPrefsPath, addressBookPath);
        logger = LogsCenter.getLogger(MainApp.class);
        logger.addHandler(logHandler);
    }

    @AfterEach
    public void tearDown() {
        logger.removeHandler(logHandler);
    }

    @Test
    public void init_missingFiles_loadsSampleClientsAndLogsBranding() throws Exception {
        app.init();

        ReadOnlyAddressBook expectedSamples = SampleAddressBookLoader.load();
        assertEquals(expectedSamples, app.model.getAddressBook());
        assertTrue(Files.exists(addressBookPath));
        assertEquals(expectedSamples,
                new JsonAddressBookStorage(addressBookPath).readAddressBook().orElseThrow());
        assertEquals(app.model.getFilteredPersonList(), app.logic.getFilteredPersonList());
        assertNotNull(app.ui);
        assertEquals(userPrefsPath, app.storage.getUserPrefsFilePath());
        assertEquals(addressBookPath, app.storage.getAddressBookFilePath());
        assertEquals(new UserPrefs(), new JsonUserPrefsStorage(userPrefsPath).readUserPrefs().orElseThrow());
        assertTrue(logHandler.messages.stream()
                .anyMatch(message -> message.contains("Initializing EstateBookUltraProMax")));
        assertTrue(logHandler.messages.stream()
                .anyMatch(message -> message.contains("populated with sample client data.")));
    }

    @Test
    public void init_existingClients_preservesDataFile() throws Exception {
        AddressBook existing = TypicalPersons.getTypicalAddressBook();
        new JsonAddressBookStorage(addressBookPath).saveAddressBook(existing);
        String originalJson = Files.readString(addressBookPath);

        app.init();

        assertEquals(existing, app.model.getAddressBook());
        assertEquals(originalJson, Files.readString(addressBookPath));
    }

    @Test
    public void init_utf8BomDataFile_listPreservesClients() throws Exception {
        AddressBook existing = TypicalPersons.getTypicalAddressBook();
        new JsonAddressBookStorage(addressBookPath).saveAddressBook(existing);
        Files.writeString(addressBookPath, "\uFEFF" + Files.readString(addressBookPath), StandardCharsets.UTF_8);

        app.init();
        app.logic.execute("list");

        assertEquals(existing, app.model.getAddressBook());
        assertEquals(existing, new JsonAddressBookStorage(addressBookPath).readAddressBook().orElseThrow());
    }

    @Test
    public void init_existingEmptyClientBook_doesNotLoadSamples() throws Exception {
        new JsonAddressBookStorage(addressBookPath).saveAddressBook(new AddressBook());
        String originalJson = Files.readString(addressBookPath);

        app.init();

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertEquals(originalJson, Files.readString(addressBookPath));
    }

    @Test
    public void init_corruptDataFile_doesNotOverwriteFile() throws Exception {
        String invalidJson = "{invalid json";
        Files.writeString(addressBookPath, invalidJson);

        app.init();

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertEquals(invalidJson, Files.readString(addressBookPath));
        Path backup = findUnreadableDataBackup();
        assertEquals(invalidJson, Files.readString(backup));

        app.logic.execute("list");

        assertEquals(invalidJson, Files.readString(backup));
        assertEquals(new AddressBook(), new JsonAddressBookStorage(addressBookPath).readAddressBook().orElseThrow());
    }

    @Test
    public void init_v14BookWithSpaceOnlyDuplicateNames_keepsOriginalInBackupAfterNextCommand() throws Exception {
        String v14Json = Files.readString(V1_4_SPACED_NAMES_FILE);
        Files.writeString(addressBookPath, v14Json);

        app.init();

        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        Path backup = findUnreadableDataBackup();
        assertEquals(v14Json, Files.readString(backup));

        app.logic.execute(ADD_CLIENT_COMMAND);

        // the first command that changes data replaces the file, and the backup keeps the original text
        assertEquals(v14Json, Files.readString(backup));
        AddressBook afterAdd = new AddressBook(
                new JsonAddressBookStorage(addressBookPath).readAddressBook().orElseThrow());
        assertEquals(1, afterAdd.getPersonList().size());
        assertEquals("Recovered Client", afterAdd.getPersonList().get(0).getName().fullName);
    }

    @Test
    public void init_emptyDataFile_backsUpFileBeforeNextCommand() throws Exception {
        Files.createFile(addressBookPath);

        app.init();

        Path backup = findUnreadableDataBackup();
        assertEquals(0, Files.size(backup));
        app.logic.execute("list");
        assertEquals(0, Files.size(backup));
        assertEquals(new AddressBook(), new JsonAddressBookStorage(addressBookPath).readAddressBook().orElseThrow());
    }

    @Test
    public void backUpUnreadableDataFile_copyFails_removesEmptyBackup() throws Exception {
        assertThrows(IOException.class, () -> app.backUpUnreadableDataFile(addressBookPath));

        try (var files = Files.list(temporaryFolder)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().startsWith(
                    "addressbook.json.unreadable-")));
        }
    }

    @Test
    public void init_unreadableDataBackupFails_logsReasonAndPreservesFile() throws Exception {
        String invalidJson = "{invalid json";
        Files.writeString(addressBookPath, invalidJson);
        app = new MainApp(userPrefsPath, addressBookPath) {
            @Override
            Path backUpUnreadableDataFile(Path dataFilePath) throws IOException {
                throw new IOException("Simulated backup failure");
            }
        };

        IOException exception = assertThrows(IOException.class, app::init);

        assertTrue(exception.getMessage().contains("Could not back up unreadable client data file"));
        assertTrue(exception.getMessage().contains(addressBookPath.toString()));
        assertTrue(exception.getMessage().contains("Startup has stopped without changing the original file"));
        assertTrue(logHandler.messages.stream().anyMatch(message -> message.equals(exception.getMessage())));
        assertEquals(invalidJson, Files.readString(addressBookPath));
    }

    @Test
    public void init_secondStartup_preservesSavedClientChanges() throws Exception {
        app.init();
        app.logic.execute("delete 1");
        ReadOnlyAddressBook expected = new AddressBook(app.model.getAddressBook());

        MainApp restartedApp = new MainApp(userPrefsPath, addressBookPath);
        restartedApp.init();

        assertEquals(expected, restartedApp.model.getAddressBook());
        assertEquals(4, restartedApp.model.getAddressBook().getPersonList().size());
    }

    @Test
    public void start_defaultApplication_startsUiAndLogsNameAndVersion() {
        MainApp defaultApp = new MainApp();
        UiStub ui = new UiStub();
        defaultApp.ui = ui;

        defaultApp.start(null);

        assertEquals(1, ui.startCount);
        assertTrue(logHandler.messages.contains("Starting EstateBookUltraProMax V1.4"));
    }

    @Test
    public void stop_initializedApplication_savesPreferencesAndLogsBranding() throws Exception {
        app.init();
        app.model.setGuiSettings(new GuiSettings(1000, 800, 40, 50));
        logHandler.messages.clear();

        app.stop();

        assertEquals(app.model.getUserPrefs(), new JsonUserPrefsStorage(userPrefsPath).readUserPrefs().orElseThrow());
        assertTrue(logHandler.messages.stream()
                .anyMatch(message -> message.contains("Stopping EstateBookUltraProMax")));
    }

    private Path findUnreadableDataBackup() throws Exception {
        try (var files = Files.list(temporaryFolder)) {
            return files.filter(path -> path.getFileName().toString().startsWith("addressbook.json.unreadable-"))
                    .findFirst().orElseThrow();
        }
    }

    /**
     * Records UI startup without creating a JavaFX window.
     */
    private static class UiStub implements Ui {
        private int startCount;

        @Override
        public void start(Stage primaryStage) {
            startCount++;
        }
    }

    /**
     * Captures application log messages for branding assertions.
     */
    private static class LogHandler extends Handler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
            // No buffered output.
        }

        @Override
        public void close() {
            // No resources to close.
        }
    }
}
