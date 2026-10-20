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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

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
    public void layout_supportedLogicalViewports_keepsMainRegionsVisible() throws Exception {
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

                // Check logical viewport sizes only; this does not change the display scale or font metrics.
                // Actual display-scaling checks are listed in docs/Testing.md.
                int[][] logicalSizes = {{1920, 1080}, {1536, 864}, {853, 480}, {820, 440}};
                for (int[] size : logicalSizes) {
                    root.resize(size[0], size[1]);
                    root.layout();
                    for (String id : List.of("menuBar", "commandTextField", "resultDisplayPlaceholder",
                            "personList", "personListView", "detailPanePlaceholder", "detailPane", "emptyState",
                            "statusbarPlaceholder", "saveLocationStatus", "clientCountStatus")) {
                        assertInsideViewport(root.lookup("#" + id), size[0], size[1]);
                    }

                    assertEquals(340, root.lookup("#personList").getLayoutBounds().getWidth(), 1);
                    assertTrue(root.lookup("#detailPanePlaceholder").getLayoutBounds().getWidth() >= 380);
                    Label placeholder = (Label) root.lookup("#emptyState");
                    assertEquals("Select a client to see details.", placeholder.getText());
                    assertSame(window.getDetailPane().getRoot(), root.lookup("#detailPane"));
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
    public void layout_populatedDetailPaneAtSmallViewports_wrapsAndScrollsWithoutExpandingWindow() throws Exception {
        runOnFxThread(() -> {
            String noteText = "First line ".repeat(15) + "\n" + "Second line ".repeat(14) + "\n"
                    + "Third line ".repeat(15).stripTrailing() + ".";
            assertEquals(500, noteText.length());
            Person person = new PersonBuilder().withNote(noteText).withTags("urgent").build();
            ModelManager model = new ModelManager();
            model.addPerson(person);
            MainWindow window = openWindow(model);
            try {
                // Selection wiring belongs to #101; exercise the embedded panel through its public API.
                window.getDetailPane().setPerson(person);
                Region root = (Region) window.getRoot().getScene().getRoot();
                root.applyCss();
                int[][] logicalSizes = {{853, 480}, {820, 440}};
                for (int[] size : logicalSizes) {
                    layoutWindow(root, size[0], size[1]);
                    ScrollPane scroll = (ScrollPane) root.lookup("#detailScroll");
                    VBox rows = (VBox) scroll.getContent();
                    Label note = (Label) root.lookup("#note");
                    assertInsideViewport(root.lookup("#detailPane"), size[0], size[1]);
                    assertInsideViewport(root.lookup("#statusbarPlaceholder"), size[0], size[1]);
                    assertTrue(rows.getHeight() > scroll.getViewportBounds().getHeight());
                    assertTrue(rows.getWidth() <= scroll.getViewportBounds().getWidth() + 1);
                    assertEquals(noteText, note.getText());
                    assertTrue(note.getHeight() >= note.prefHeight(note.getWidth()) - 1);
                    assertTrue(note.getHeight() > 25, "The note did not wrap");
                    assertFalse(root.lookup("#emptyState").isVisible());

                    scroll.setVvalue(1);
                    layoutWindow(root, size[0], size[1]);
                    Node viewport = scroll.lookup(".viewport");
                    Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
                    Node tagsRow = root.lookup("#tagsRow");
                    Bounds lastRow = tagsRow.localToScene(tagsRow.getBoundsInLocal());
                    assertTrue(lastRow.getMaxY() <= viewportBounds.getMaxY() + 1,
                            "The final detail row is unreachable");
                    assertTrue(lastRow.getMaxY() > viewportBounds.getMinY());
                }

                window.getDetailPane().setPerson(null);
                assertTrue(root.lookup("#emptyState").isVisible());
                assertFalse(root.lookup("#detailScroll").isManaged());
                assertEquals("", ((Label) root.lookup("#note")).getText());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void layout_compactMainWindow_showsThreeCompleteClientCards() throws Exception {
        runOnFxThread(() -> {
            List<Person> clients = List.of(
                    new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                            .withPhone("90000001").withEmail("alexandra.elizabeth.montgomery@example.com")
                            .withRole(ClientRole.BOTH)
                            .withTags("firsttime", "urgent").build(),
                    new PersonBuilder().withName("Alicia Tan").withPhone("90000002")
                            .withEmail("alicia@example.com").withRole(ClientRole.BUYER)
                            .withTags("firsttime").build(),
                    new PersonBuilder().withName("Priya Nair").withPhone("90000003")
                            .withEmail("priya@example.com").withRole(ClientRole.SELLER).withTags().build(),
                    new PersonBuilder().withName("Ravi Kumar").withPhone("90000004")
                            .withEmail("ravi@example.com").withRole(ClientRole.BUYER).build(),
                    new PersonBuilder().withName("Cheryl Goh").withPhone("90000005")
                            .withEmail("cheryl@example.com").withRole(ClientRole.BOTH).build());
            ModelManager model = new ModelManager();
            clients.forEach(model::addPerson);
            MainWindow window = openWindow(model);
            try {
                // Exercise the complete window at a compact logical size, not actual OS display scaling.
                Region root = (Region) window.getRoot().getScene().getRoot();
                root.resize(853, 480);
                root.applyCss();
                root.layout();

                ListView<?> personList = (ListView<?>) root.lookup("#personListView");
                Bounds listBounds = personList.localToScene(personList.getLayoutBounds());
                List<ListCell<?>> firstThree = personList.lookupAll(".list-cell").stream()
                        .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getIndex() < 3)
                        .<ListCell<?>>map(node -> (ListCell<?>) node).toList();
                assertEquals(3, firstThree.size());
                for (ListCell<?> cell : firstThree) {
                    Bounds bounds = cell.localToScene(cell.getLayoutBounds());
                    assertTrue(bounds.getMinY() >= listBounds.getMinY());
                    assertTrue(bounds.getMaxY() <= listBounds.getMaxY());
                    Region card = (Region) cell.getGraphic();
                    Label name = (Label) card.lookup("#name");
                    assertEquals(70, card.getHeight(), 0.01);
                    assertEquals(clients.get(cell.getIndex()).getName().fullName, name.getText());
                    assertFalse(name.isWrapText());
                    assertFalse(((Label) card.lookup("#email")).isWrapText());
                    assertEquals(3, ((VBox) card.lookup("#cardContent")).getChildren().size());
                }
                for (String id : List.of("commandTextField", "resultDisplayPlaceholder", "personList",
                        "detailPanePlaceholder", "emptyState", "clientCountStatus")) {
                    assertInsideViewport(root.lookup("#" + id), 853, 480);
                }
                Label placeholder = (Label) root.lookup("#emptyState");
                assertEquals("Select a client to see details.", placeholder.getText());
                assertEquals("5 clients", ((Label) root.lookup("#clientCountStatus")).getText());
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
                assertEquals("Showing 1 of 2 clients.", feedback.getText());
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
                assertEquals("Showing 0 of 2 clients.", feedback.getText());
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
                assertEquals("1 client", count.getText());

                enterCommand(window, commandBox,
                        "add n/Benjamin Lim p/82345678 e/benjamin.lim@example.com a/Tampines");
                assertEquals("2 clients", count.getText());

                enterCommand(window, commandBox, "find Alicia");
                assertEquals("1 client", count.getText());

                enterCommand(window, commandBox, "find Nobody");
                assertEquals("0 clients", count.getText());

                enterCommand(window, commandBox, "list");
                assertEquals("2 clients", count.getText());

                enterCommand(window, commandBox, "delete 1");
                assertEquals("1 client", count.getText());

                enterCommand(window, commandBox, "clear");
                assertEquals("1 client", count.getText());
                enterCommand(window, commandBox, "clear confirm");
                assertEquals("0 clients", count.getText());
                assertEquals(dataFile.toString(), saveLocation.getText());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void execute_clearThenConfirm_showsPendingStateUntilConfirmed() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                ListView<?> personList = (ListView<?>) window.getRoot().getScene().lookup("#personListView");
                assertPendingState(commandBox, feedback, false);

                enterCommand(window, commandBox, "clear");
                assertEquals("This will delete all 2 clients. Type clear confirm to proceed.", feedback.getText());
                assertPendingState(commandBox, feedback, true);
                assertFeedbackFill(feedback, Color.web("#e0a526"));
                assertEquals(List.of(AMY, BOB), personList.getItems());

                enterCommand(window, commandBox, "clear confirm");
                assertEquals(ClearCommand.MESSAGE_SUCCESS, feedback.getText());
                assertPendingState(commandBox, feedback, false);
                assertFeedbackFill(feedback, Color.WHITE);
                assertEquals(List.of(), personList.getItems());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void execute_clearThenOtherInput_cancelsPendingState() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                ListView<?> personList = (ListView<?>) window.getRoot().getScene().lookup("#personListView");

                enterCommand(window, commandBox, "clear");
                assertPendingState(commandBox, feedback, true);

                commandBox.setText("something else");
                assertPendingState(commandBox, feedback, true);

                enterCommand(window, commandBox, "something else");
                assertEquals(ClearCommand.MESSAGE_CANCELLED, feedback.getText());
                assertPendingState(commandBox, feedback, false);
                assertFeedbackFill(feedback, Color.WHITE);
                assertEquals(List.of(AMY), personList.getItems());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void execute_clearThenEmptyEnterThenSpace_staysPendingThenCancels() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                ListView<?> personList = (ListView<?>) window.getRoot().getScene().lookup("#personListView");

                enterCommand(window, commandBox, "clear");
                assertPendingState(commandBox, feedback, true);

                // An empty command box is not an input, so the confirmation stays pending.
                enterCommand(window, commandBox, "");
                assertEquals(ClearCommand.MESSAGE_WARNING_ONE_CLIENT, feedback.getText());
                assertPendingState(commandBox, feedback, true);
                assertEquals(List.of(AMY), personList.getItems());

                // A single space is an input, so it cancels the confirmation without deleting anyone.
                enterCommand(window, commandBox, " ");
                assertEquals(ClearCommand.MESSAGE_CANCELLED, feedback.getText());
                assertPendingState(commandBox, feedback, false);
                assertEquals(List.of(AMY), personList.getItems());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void execute_clearConfirmWithoutPendingClear_showsErrorNotPendingState() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");

                enterCommand(window, commandBox, "clear confirm");

                assertEquals("Error: " + ClearCommand.MESSAGE_NO_CLEAR_PENDING, feedback.getText());
                assertTrue(feedback.getStyleClass().contains("error"));
                assertPendingState(commandBox, feedback, false);
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

    private static void layoutWindow(Region root, double width, double height) {
        root.resize(width, height);
        root.applyCss();
        // Wrapping and scrollbar visibility can request another layout pass.
        for (int pass = 0; pass < 10 && root.isNeedsLayout(); pass++) {
            root.layout();
        }
        assertFalse(root.isNeedsLayout(), "The main window layout did not settle");
    }

    private MainWindow openWindow(ModelManager model) {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), dataFile);
        window.fillInnerParts();
        return window;
    }

    /**
     * Verifies that the command box and result display either both show the pending style or both do not.
     */
    private static void assertPendingState(TextField commandBox, TextArea feedback, boolean isPending) {
        assertEquals(isPending, commandBox.getStyleClass().contains("pending"));
        assertEquals(isPending, feedback.getStyleClass().contains("pending"));
        assertEquals(isPending ? CommandBox.PENDING_PROMPT_TEXT : "Enter command here...",
                commandBox.getPromptText());
    }

    private static void assertFeedbackFill(TextArea feedback, Color expectedFill) {
        assertEquals(expectedFill, ((Text) feedback.lookup(".text")).getFill());
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
