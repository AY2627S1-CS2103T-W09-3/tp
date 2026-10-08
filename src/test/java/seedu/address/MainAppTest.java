package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
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
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.ui.Ui;

public class MainAppTest {

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

        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
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
    public void start_defaultApplication_startsUiAndLogsNameAndVersion() {
        MainApp defaultApp = new MainApp();
        UiStub ui = new UiStub();
        defaultApp.ui = ui;

        defaultApp.start(null);

        assertEquals(1, ui.startCount);
        assertTrue(logHandler.messages.contains("Starting EstateBookUltraProMax V1.2"));
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
