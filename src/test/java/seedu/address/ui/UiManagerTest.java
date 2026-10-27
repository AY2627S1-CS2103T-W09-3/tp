package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests startup warnings shown by the UI manager. */
public class UiManagerTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialise = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialise);
        } catch (IllegalStateException e) {
            Platform.runLater(initialise);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void start_unreadableDataBackup_showsRecoveryWarning() throws Exception {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        Path backup = temporaryFolder.resolve("addressbook.json.unreadable-test.bak");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        FutureTask<RecordingUiManager> startUi = new FutureTask<>(() -> {
            Stage stage = new Stage();
            RecordingUiManager ui = new RecordingUiManager(new LogicManager(new ModelManager(), storage),
                    dataFile, backup);
            try {
                ui.start(stage);
                return ui;
            } finally {
                stage.close();
            }
        });

        Platform.runLater(startUi);
        RecordingUiManager ui = startUi.get(10, TimeUnit.SECONDS);

        assertEquals(AlertType.WARNING, ui.alertType);
        assertEquals("Client data could not be loaded", ui.alertTitle);
        assertEquals("The data file could not be read. An empty client book was opened.", ui.alertHeader);
        assertTrue(ui.alertContent.contains(backup.toString()));
        assertTrue(ui.alertContent.contains("Please keep this backup"));
    }

    private static class RecordingUiManager extends UiManager {
        private AlertType alertType;
        private String alertTitle;
        private String alertHeader;
        private String alertContent;

        RecordingUiManager(LogicManager logic, Path dataFile, Path backup) {
            super(logic, dataFile, backup);
        }

        @Override
        void showAlertDialogAndWait(AlertType type, String title, String headerText, String contentText) {
            alertType = type;
            alertTitle = title;
            alertHeader = headerText;
            alertContent = contentText;
        }
    }
}
