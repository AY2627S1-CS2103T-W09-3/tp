package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests command feedback and the displayed client list through the actual JavaFX command box.
 */
public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    private MainWindow window;
    private TextField commandBox;
    private TextArea resultDisplay;
    private ListView<?> personList;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX did not start in time");
    }

    @BeforeEach
    public void setUp() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            Path dataFile = temporaryFolder.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            window = new MainWindow(new Stage(), new LogicManager(model, storage), dataFile);
            window.fillInnerParts();
            commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
            resultDisplay = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
            personList = (ListView<?>) window.getRoot().getScene().lookup("#personListView");
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        runOnFxThread(() -> {
            if (window != null) {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void execute_emptyFind_showsRedErrorAndKeepsList() throws Exception {
        runOnFxThread(() -> {
            enterCommand("find aMY");
            assertEquals("1 clients listed!", resultDisplay.getText());
            assertEquals(List.of(AMY), personList.getItems());

            String expectedMessage = "Invalid command format!\n"
                    + "find: Finds clients by name.\n"
                    + "Parameters: KEYWORD [MORE_KEYWORDS]\n"
                    + "Example: find John";
            for (String command : List.of("find", "find   \t")) {
                enterCommand(command);
                assertEquals(expectedMessage, resultDisplay.getText());
                assertEquals(List.of(AMY), personList.getItems());
                assertErrorStyle();
            }

            enterCommand("list");
            assertEquals(ListCommand.MESSAGE_SUCCESS, resultDisplay.getText());
            assertEquals(List.of(AMY, BOB), personList.getItems());
            assertSuccessStyle();
        });
    }

    @Test
    public void execute_commandFailureThenFind_clearsErrorStyle() throws Exception {
        runOnFxThread(() -> {
            enterCommand("delete 9");
            assertEquals(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, resultDisplay.getText());
            assertEquals(List.of(AMY, BOB), personList.getItems());
            assertErrorStyle();

            enterCommand("find Nobody");
            assertEquals("0 clients listed!", resultDisplay.getText());
            assertEquals(List.of(), personList.getItems());
            assertSuccessStyle();
        });
    }

    private void enterCommand(String command) {
        commandBox.setText(command);
        commandBox.fireEvent(new ActionEvent());
        window.getRoot().getScene().getRoot().applyCss();
    }

    private void assertErrorStyle() {
        assertEquals(1, Collections.frequency(resultDisplay.getStyleClass(), "error"));
        Text feedbackText = (Text) resultDisplay.lookup(".text");
        assertEquals(Color.web("#d06651"), feedbackText.getFill());
    }

    private void assertSuccessStyle() {
        assertFalse(resultDisplay.getStyleClass().contains("error"));
        Text feedbackText = (Text) resultDisplay.lookup(".text");
        assertEquals(Color.WHITE, feedbackText.getFill());
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
