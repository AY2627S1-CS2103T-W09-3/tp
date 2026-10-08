package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests error feedback and recovery through the command box.
 */
public class MainWindowTest {

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
            // Another UI test has already started JavaFX.
            Platform.runLater(initialise);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void execute_parseFailureThenSuccess_resetsErrorFeedback() throws Exception {
        assertFailureAndRecovery("find", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void execute_commandFailureThenSuccess_resetsErrorFeedback() throws Exception {
        assertFailureAndRecovery("delete 1", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    private void assertFailureAndRecovery(String command, String expectedError) throws Exception {
        runOnFxThread(() -> {
            Path dataFile = temporaryFolder.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(new ModelManager(), storage), dataFile);
            try {
                window.fillInnerParts();
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");

                for (int attempt = 0; attempt < 2; attempt++) {
                    enterCommand(window, commandBox, command);
                    assertEquals("Error: " + expectedError, feedback.getText());
                    assertEquals(1, Collections.frequency(feedback.getStyleClass(), "error"));
                    assertEquals(Color.web("#d06651"), ((Text) feedback.lookup(".text")).getFill());
                }

                enterCommand(window, commandBox, "list");
                assertEquals(ListCommand.MESSAGE_SUCCESS, feedback.getText());
                assertFalse(feedback.getStyleClass().contains("error"));
                assertEquals(Color.WHITE, ((Text) feedback.lookup(".text")).getFill());
            } finally {
                window.getRoot().close();
            }
        });
    }

    private static void enterCommand(MainWindow window, TextField commandBox, String command) {
        commandBox.setText(command);
        commandBox.fireEvent(new ActionEvent());
        window.getRoot().getScene().getRoot().applyCss();
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
