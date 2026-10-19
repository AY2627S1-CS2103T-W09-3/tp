package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
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
 * Tests the main window layout, startup focus, and command-box feedback.
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
    public void show_commandBoxHasInitialFocus() throws Exception {
        FutureTask<MainWindow> createWindow = new FutureTask<>(() -> {
            Path dataFile = temporaryFolder.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(new ModelManager(), storage), dataFile);
            window.show();
            window.fillInnerParts();
            return window;
        });
        Platform.runLater(createWindow);
        MainWindow window = createWindow.get(10, TimeUnit.SECONDS);
        try {
            runOnFxThread(() -> {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                assertSame(commandBox, window.getRoot().getScene().getFocusOwner());
                assertTrue(commandBox.isFocused());
            });
        } finally {
            runOnFxThread(() -> window.getRoot().close());
        }
    }

    @Test
    public void layout_supportedLogicalSizes_keepsMainRegionsVisible() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            Path dataFile = temporaryFolder.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), dataFile);
            try {
                window.fillInnerParts();
                Region root = (Region) window.getRoot().getScene().getRoot();
                root.applyCss();

                // Effective content sizes for 100%, 125%, and 150% scaling, plus the window minimum.
                int[][] logicalSizes = {{1920, 1080}, {1536, 864}, {853, 480}, {820, 440}};
                for (int[] size : logicalSizes) {
                    root.resize(size[0], size[1]);
                    root.layout();
                    for (String id : List.of("menuBar", "commandTextField", "resultDisplayPlaceholder",
                            "personList", "personListView", "detailPanePlaceholder", "detailPlaceholderMessage",
                            "statusbarPlaceholder", "saveLocationStatus", "clientCountStatus")) {
                        assertInsideViewport(root.lookup("#" + id), size[0], size[1]);
                    }

                    assertEquals(340, root.lookup("#personList").getLayoutBounds().getWidth(), 1);
                    assertTrue(root.lookup("#detailPanePlaceholder").getLayoutBounds().getWidth() >= 380);
                    Label count = (Label) root.lookup("#clientCountStatus");
                    assertEquals("2 clients", count.getText());
                    assertTrue(count.getWidth() >= count.prefWidth(-1) - 1);
                    Node saveLocation = root.lookup("#saveLocationStatus");
                    assertTrue(saveLocation.localToScene(saveLocation.getLayoutBounds()).getMaxX()
                            <= count.localToScene(count.getLayoutBounds()).getMinX() + 1);
                }
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void execute_parseFailureThenSuccess_resetsErrorFeedback() throws Exception {
        assertFailureAndRecovery("find", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void execute_commandFailureThenSuccess_resetsErrorFeedback() throws Exception {
        assertFailureAndRecovery("delete 1", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_findWithoutKeywords_keepsFilteredListUntilListCommand() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            Path dataFile = temporaryFolder.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), dataFile);
            try {
                window.fillInnerParts();
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                ListView<?> personList = (ListView<?>) window.getRoot().getScene().lookup("#personListView");

                enterCommand(window, commandBox, "find aMY");
                assertEquals("1 client listed!", feedback.getText());
                assertEquals(List.of(AMY), personList.getItems());

                String expectedError = "Error: "
                        + String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
                for (String command : List.of("find", "find   \t")) {
                    enterCommand(window, commandBox, command);
                    assertEquals(expectedError, feedback.getText());
                    assertEquals(List.of(AMY), personList.getItems());
                    assertTrue(feedback.getStyleClass().contains("error"));
                }

                enterCommand(window, commandBox, "list");
                assertEquals(ListCommand.MESSAGE_SUCCESS, feedback.getText());
                assertEquals(List.of(AMY, BOB), personList.getItems());
                assertFalse(feedback.getStyleClass().contains("error"));

                enterCommand(window, commandBox, "find Nobody");
                assertEquals("No clients found", feedback.getText());
                assertFalse(feedback.getStyleClass().contains("error"));
                assertEquals(List.of(), personList.getItems());

                enterCommand(window, commandBox, "list");
                assertEquals(List.of(AMY, BOB), personList.getItems());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void execute_clientListChanges_updatesFooterCount() throws Exception {
        runOnFxThread(() -> {
            Path dataFile = temporaryFolder.resolve("addressbook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(new ModelManager(), storage), dataFile);
            try {
                window.fillInnerParts();
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                Label count = (Label) window.getRoot().getScene().lookup("#clientCountStatus");
                Label saveLocation = (Label) window.getRoot().getScene().lookup("#saveLocationStatus");

                assertEquals(dataFile.toString(), saveLocation.getText());
                assertEquals("0 clients", count.getText());

                enterCommand(window, commandBox,
                        "add n/Alicia Tan p/81234567 e/alicia.tan@example.com a/Bishan");
                assertEquals("1 clients", count.getText());

                enterCommand(window, commandBox,
                        "add n/Benjamin Lim p/82345678 e/benjamin.lim@example.com a/Tampines");
                assertEquals("2 clients", count.getText());

                enterCommand(window, commandBox, "find Alicia");
                assertEquals("1 clients", count.getText());

                enterCommand(window, commandBox, "find Nobody");
                assertEquals("0 clients", count.getText());

                enterCommand(window, commandBox, "list");
                assertEquals("2 clients", count.getText());

                enterCommand(window, commandBox, "delete 1");
                assertEquals("1 clients", count.getText());

                enterCommand(window, commandBox, "clear");
                assertEquals("0 clients", count.getText());
                assertEquals(dataFile.toString(), saveLocation.getText());
            } finally {
                window.getRoot().close();
            }
        });
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

    private static void assertInsideViewport(Node node, double width, double height) {
        Bounds bounds = node.localToScene(node.getLayoutBounds());
        String context = node.getId() + " at " + width + "x" + height;
        assertTrue(bounds.getWidth() > 0 && bounds.getHeight() > 0, context + " is collapsed");
        assertTrue(bounds.getMinX() >= -1 && bounds.getMinY() >= -1
                && bounds.getMaxX() <= width + 1 && bounds.getMaxY() <= height + 1,
                context + " extends beyond the viewport");
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
