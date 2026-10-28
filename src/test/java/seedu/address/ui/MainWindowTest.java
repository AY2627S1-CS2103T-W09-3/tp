package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.model.history.CommandHistory.MESSAGE_NOTHING_TO_REDO;
import static seedu.address.model.history.CommandHistory.MESSAGE_NOTHING_TO_UNDO;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.ui.PersonCardTestUtil.assertCardContentFits;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Budget;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonMatchesFilterPredicate;
import seedu.address.model.person.Price;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the main window layout, startup focus, selection, and command-box feedback.
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
    public void historyMenuAndShortcuts_executeThroughCommandBox() throws Exception {
        runOnFxThread(() -> {
            RecordingLogic logic = new RecordingLogic();
            MainWindow window = new MainWindow(new Stage(), logic, temporaryFolder.resolve("addressbook.json"));
            try {
                window.fillInnerParts();
                window.show();
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                MenuBar menuBar = (MenuBar) window.getRoot().getScene().lookup("#menuBar");
                List<MenuItem> editItems = menuBar.getMenus().get(1).getItems();
                assertEquals(List.of("Undo", "Redo", "History"),
                        editItems.stream().map(MenuItem::getText).toList());

                commandBox.setText("unfinished command");
                editItems.get(0).fire();
                editItems.get(1).fire();
                editItems.get(2).fire();
                assertEquals(List.of("undo", "redo", "history"), logic.executedCommands);
                assertEquals("Executed history", feedback.getText());
                assertEquals("unfinished command", commandBox.getText());

                commandBox.requestFocus();
                assertSame(commandBox, window.getRoot().getScene().getFocusOwner());
                fireShortcut(commandBox, KeyCode.Z, false, true, false);
                fireShortcut(commandBox, KeyCode.Z, false, false, true);
                fireShortcut(commandBox, KeyCode.Y, false, true, false);
                fireShortcut(commandBox, KeyCode.Z, true, false, true);
                assertEquals(List.of("undo", "redo", "history", "undo", "undo", "redo", "redo"),
                        logic.executedCommands);
                assertEquals("Executed redo", feedback.getText());
                assertEquals("unfinished command", commandBox.getText());

                // Unrelated or modified key presses must not trigger history commands.
                commandBox.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.Z,
                        false, true, true, false));
                fireShortcut(commandBox, KeyCode.Z, true, true, false);
                fireShortcut(commandBox, KeyCode.A, false, false, false);
                fireShortcut(commandBox, KeyCode.Z, false, true, true);
                fireShortcut(commandBox, KeyCode.Z, false, false, false);
                fireShortcut(commandBox, KeyCode.Y, false, false, false);
                fireShortcut(commandBox, KeyCode.Y, true, true, false);
                fireShortcut(commandBox, KeyCode.Z, true, false, false);
                assertEquals(List.of("undo", "redo", "history", "undo", "undo", "redo", "redo"),
                        logic.executedCommands);
                assertEquals("unfinished command", commandBox.getText());

                enterCommand(window, commandBox, "list");
                assertEquals("list", logic.executedCommands.get(logic.executedCommands.size() - 1));
                assertEquals("", commandBox.getText());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void historyMenuItems_followUndoAndRedoAvailability() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                MenuBar menuBar = (MenuBar) window.getRoot().getScene().lookup("#menuBar");
                MenuItem undoItem = menuBar.getMenus().get(1).getItems().get(0);
                MenuItem redoItem = menuBar.getMenus().get(1).getItems().get(1);
                assertTrue(undoItem.isDisable());
                assertTrue(redoItem.isDisable());

                enterCommand(window, commandBox, "delete 1");
                assertFalse(undoItem.isDisable());
                assertTrue(redoItem.isDisable());

                undoItem.fire();
                assertTrue(undoItem.isDisable());
                assertFalse(redoItem.isDisable());

                redoItem.fire();
                assertFalse(undoItem.isDisable());
                assertTrue(redoItem.isDisable());

                enterCommand(window, commandBox, "undo");
                enterCommand(window, commandBox, "delete 2");
                assertFalse(undoItem.isDisable());
                assertTrue(redoItem.isDisable());

                enterCommand(window, commandBox, "delete 99");
                assertFalse(undoItem.isDisable());
                assertTrue(redoItem.isDisable());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void historyShortcuts_whenMenuItemsDisabled_showEmptyHistoryFeedback() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = openWindow(new ModelManager());
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                MenuBar menuBar = (MenuBar) window.getRoot().getScene().lookup("#menuBar");
                MenuItem undoItem = menuBar.getMenus().get(1).getItems().get(0);
                MenuItem redoItem = menuBar.getMenus().get(1).getItems().get(1);
                assertTrue(undoItem.isDisable());
                assertTrue(redoItem.isDisable());

                fireShortcut(commandBox, KeyCode.Z, false, true, false);
                assertEquals("Error: " + MESSAGE_NOTHING_TO_UNDO, feedback.getText());
                fireShortcut(commandBox, KeyCode.Y, false, true, false);
                assertEquals("Error: " + MESSAGE_NOTHING_TO_REDO, feedback.getText());
                fireShortcut(commandBox, KeyCode.Z, true, false, true);
                assertEquals("Error: " + MESSAGE_NOTHING_TO_REDO, feedback.getText());
                assertTrue(undoItem.isDisable());
                assertTrue(redoItem.isDisable());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void redoAcceleratorFor_usesPlatformShortcut() {
        assertEquals(KeyCombination.valueOf("Meta+Shift+Z"), MainWindow.redoAcceleratorFor("Mac OS X"));
        assertEquals(KeyCombination.valueOf("Ctrl+Y"), MainWindow.redoAcceleratorFor("Linux"));
        assertEquals(KeyCombination.valueOf("Ctrl+Y"), MainWindow.redoAcceleratorFor("Windows 11"));
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
                    layoutWindow(root, size[0], size[1]);
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
    public void execute_noteAddedThenCleared_updatesCardMarker() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().build());
            MainWindow window = openWindow(model);
            try {
                Region root = (Region) window.getRoot().getScene().getRoot();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                layoutWindow(root, 820, 440);
                assertFalse(root.lookup("#noteIndicator").isManaged());

                enterCommand(window, commandBox, "note 1 Call next week");
                layoutWindow(root, 820, 440);
                assertTrue(root.lookup("#noteIndicator").isVisible());
                assertTrue(root.lookup("#noteIndicator").isManaged());
                assertTrue(root.lookup("#noteRow").isVisible());
                assertEquals("Call next week", ((Label) root.lookup("#note")).getText());

                enterCommand(window, commandBox, "note 1 clear");
                layoutWindow(root, 820, 440);
                assertFalse(((TextArea) root.lookup("#resultDisplay")).getStyleClass().contains("error"));
                assertFalse(root.lookup("#noteIndicator").isVisible());
                assertFalse(root.lookup("#noteIndicator").isManaged());
                assertFalse(root.lookup("#noteRow").isVisible());
                assertFalse(root.lookup("#noteRow").isManaged());
                assertEquals("", ((Label) root.lookup("#note")).getText());
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
                Region root = (Region) window.getRoot().getScene().getRoot();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                enterCommand(window, commandBox, "view 1");
                assertSelectedClient(window, person);
                root.applyCss();
                int[][] logicalSizes = {{853, 480}, {820, 440}};
                for (int[] size : logicalSizes) {
                    layoutWindow(root, size[0], size[1]);
                    ScrollPane scroll = (ScrollPane) root.lookup("#detailScroll");
                    VBox rows = (VBox) scroll.getContent();
                    Label note = (Label) root.lookup("#note");
                    assertTrue(root.lookup("#noteIndicator").isVisible());
                    assertInsideViewport(root.lookup("#noteIndicator"), size[0], size[1]);
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
                    assertCardContentFits(card);
                    assertEquals(clients.get(cell.getIndex()).getName().fullName, name.getText());
                    assertFalse(name.isWrapText());
                    assertFalse(((Label) card.lookup("#email")).isWrapText());
                    assertEquals(3, ((VBox) card.lookup("#cardContent")).getChildren().stream()
                            .filter(Node::isManaged).count());
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
    public void layout_compactMainWindow_requirementsCardsFitAndLastCardIsReachable() throws Exception {
        runOnFxThread(() -> {
            List<Person> clients = List.of(
                    new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                            .withPhone("90000001").withEmail("alexandra.elizabeth.montgomery@example.com")
                            .withRole(ClientRole.BUYER).withTags("firsttime", "urgent")
                            .withBudget(new Budget(new Price("800000"), new Price("1200000"))).build(),
                    new PersonBuilder().withName("Priya Nair").withPhone("90000002")
                            .withEmail("priya@example.com").withRole(ClientRole.SELLER).withTags()
                            .withAskingPrice(new Price("950000")).build(),
                    new PersonBuilder().withName("Cheryl Goh").withPhone("90000003")
                            .withEmail("cheryl@example.com").withRole(ClientRole.BOTH).withTags("upgrader")
                            .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                            .withAskingPrice(new Price("1500000")).build());
            ModelManager model = new ModelManager();
            clients.forEach(model::addPerson);
            MainWindow window = openWindow(model);
            try {
                // This is a logical-size check, not an actual OS display-scaling check.
                Region root = (Region) window.getRoot().getScene().getRoot();
                root.resize(853, 480);
                root.applyCss();
                root.layout();

                ListView<?> personList = (ListView<?>) root.lookup("#personListView");
                Node viewport = personList.lookup(".clipped-container");
                Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
                // Requirement cards have an extra row: two must fit fully; remaining cards are scrollable.
                List<ListCell<?>> firstTwo = personList.lookupAll(".list-cell").stream()
                        .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getIndex() < 2)
                        .<ListCell<?>>map(node -> (ListCell<?>) node).toList();
                assertEquals(2, firstTwo.size());
                for (ListCell<?> cell : firstTwo) {
                    Bounds bounds = cell.localToScene(cell.getLayoutBounds());
                    assertTrue(bounds.getMinY() >= viewportBounds.getMinY() - 1);
                    assertTrue(bounds.getMaxY() <= viewportBounds.getMaxY() + 1);
                    Region card = (Region) cell.getGraphic();
                    assertCardContentFits(card);
                    assertTrue(card.lookup("#requirementsSummary").isVisible());
                    assertEquals(4, ((VBox) card.lookup("#cardContent")).getChildren().stream()
                            .filter(Node::isManaged).count());
                }

                List<Double> originalHeights = firstTwo.stream()
                        .map(cell -> cell.getGraphic().getLayoutBounds().getHeight()).toList();
                personList.getSelectionModel().select(0);
                root.applyCss();
                root.layout();
                // Newly recreated cell graphics need CSS before their content-sized heights are measured.
                root.applyCss();
                root.layout();
                for (int i = 0; i < firstTwo.size(); i++) {
                    double selectedHeight = firstTwo.get(i).getGraphic().getLayoutBounds().getHeight();
                    assertEquals(originalHeights.get(i), selectedHeight, 0.01);
                }
                personList.scrollTo(clients.size() - 1);
                root.layout();
                root.applyCss();
                root.layout();
                ListCell<?> lastCell = personList.lookupAll(".list-cell").stream()
                        .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty()
                                && cell.getIndex() == clients.size() - 1)
                        .map(node -> (ListCell<?>) node).findFirst().orElseThrow();
                Bounds lastBounds = lastCell.localToScene(lastCell.getLayoutBounds());
                assertTrue(lastBounds.getMinY() >= viewportBounds.getMinY() - 1);
                assertTrue(lastBounds.getMaxY() <= viewportBounds.getMaxY() + 1);
                Label summary = (Label) lastCell.getGraphic().lookup("#requirementsSummary");
                assertTrue(summary.isVisible());
                assertTrue(summary.getText().contains("Budget ") && summary.getText().contains("Asking "));
                assertTrue(summary.localToScene(summary.getLayoutBounds()).getMaxY() <= viewportBounds.getMaxY() + 1);
                for (String id : List.of("commandTextField", "resultDisplayPlaceholder", "detailPanePlaceholder",
                        "saveLocationStatus", "clientCountStatus")) {
                    assertInsideViewport(root.lookup("#" + id), 853, 480);
                }
                assertEquals("3 clients", ((Label) root.lookup("#clientCountStatus")).getText());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void layout_shortContentViewports_showThreeCompleteRequirementsCards() throws Exception {
        runOnFxThread(() -> {
            List<Person> clients = List.of(
                    new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                            .withPhone("90000001").withEmail("alexandra.elizabeth.montgomery@example.com")
                            .withRole(ClientRole.BUYER).withTags("firsttime", "urgent")
                            .withBudget(new Budget(new Price("800000"), new Price("1200000"))).build(),
                    new PersonBuilder().withName("Priya Nair").withRole(ClientRole.SELLER).withTags()
                            .withPhone("90000002").withEmail("priya@example.com")
                            .withAskingPrice(new Price("950000")).build(),
                    new PersonBuilder().withName("Cheryl Goh").withRole(ClientRole.BOTH).withTags("upgrading")
                            .withPhone("90000003")
                            .withEmail("cheryl.goh.long.email@example.com")
                            .withBudget(new Budget(new Price("700000"), new Price("900000")))
                            .withAskingPrice(new Price("850000")).build(),
                    new PersonBuilder().withName("Ravi Kumar").withTags()
                            .withPhone("90000004").withEmail("ravi@example.com").build());
            ModelManager model = new ModelManager();
            clients.forEach(model::addPerson);
            MainWindow window = openWindow(model);
            try {
                window.getDetailPane().setPerson(clients.get(2));
                Region root = (Region) window.getRoot().getScene().getRoot();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                // Reserve space for window decorations and desktop controls instead of using the full screen height.
                // These logical content sizes are not a substitute for actual display-scaling checks in #134.
                int[][] contentSizes = {{820, 400}, {820, 412}, {853, 400}, {853, 452}, {853, 480}};
                for (int[] size : contentSizes) {
                    list.scrollTo(0);
                    layoutWindow(root, size[0], size[1]);
                    Bounds viewport = list.lookup(".clipped-container")
                            .localToScene(list.lookup(".clipped-container").getLayoutBounds());
                    List<ListCell<?>> firstThree = list.lookupAll(".list-cell").stream()
                            .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getIndex() < 3)
                            .<ListCell<?>>map(node -> (ListCell<?>) node).toList();
                    assertEquals(3, firstThree.size());
                    for (ListCell<?> cell : firstThree) {
                        Bounds bounds = cell.localToScene(cell.getLayoutBounds());
                        assertTrue(bounds.getMinY() >= viewport.getMinY() - 1);
                        assertTrue(bounds.getMaxY() <= viewport.getMaxY() + 1,
                                "Card " + (cell.getIndex() + 1) + " is clipped at " + size[0] + "x" + size[1]);
                        Region card = (Region) cell.getGraphic();
                        assertTrue(card.lookup("#requirementsSummary").isVisible());
                        assertCardContentFits(card);
                    }
                    for (String id : List.of("menuBar", "commandTextField", "resultDisplay", "personList",
                            "detailPane", "saveLocationStatus", "clientCountStatus")) {
                        assertInsideViewport(root.lookup("#" + id), size[0], size[1]);
                    }
                    assertWithinRegion(root.lookup("#commandTextField"), root.lookup("#commandBoxPlaceholder"));
                    assertWithinRegion(root.lookup("#resultDisplay"), root.lookup("#resultDisplayPlaceholder"));
                    Node result = root.lookup("#resultDisplayPlaceholder");
                    assertTrue(viewport.getMinY() >= result.localToScene(result.getLayoutBounds()).getMaxY());

                    list.scrollTo(clients.size() - 1);
                    layoutWindow(root, size[0], size[1]);
                    ListCell<?> last = list.lookupAll(".list-cell").stream()
                            .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty()
                                    && cell.getIndex() == clients.size() - 1)
                            .map(node -> (ListCell<?>) node).findFirst().orElseThrow();
                    Bounds lastBounds = last.localToScene(last.getLayoutBounds());
                    assertTrue(lastBounds.getMinY() >= viewport.getMinY() - 1);
                    assertTrue(lastBounds.getMaxY() <= viewport.getMaxY() + 1);
                }
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void layout_resizeShortThenTall_preservesFeedbackAndRestoresResultSpace() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = openWindow(new ModelManager());
            try {
                Region root = (Region) window.getRoot().getScene().getRoot();
                TextArea feedback = (TextArea) root.lookup("#resultDisplay");
                String message = "Long feedback ".repeat(30) + "\nSecond line\nThird line";
                feedback.setText(message);
                layoutWindow(root, 1120, 800);
                double normalHeight = root.lookup("#resultDisplayPlaceholder").getLayoutBounds().getHeight();

                layoutWindow(root, 820, 400);
                assertTrue(root.lookup("#resultDisplayPlaceholder").getLayoutBounds().getHeight() < normalHeight);
                assertEquals(message, feedback.getText());
                Node viewport = feedback.lookup(".viewport");
                Text sample = new Text("Ag");
                sample.setFont(feedback.getFont());
                assertTrue(viewport.getLayoutBounds().getHeight() >= sample.getLayoutBounds().getHeight(),
                        "At least one full line of feedback must remain readable with scrollbars present");
                assertInsideViewport(feedback, 820, 400);
                feedback.setScrollTop(Double.MAX_VALUE);
                layoutWindow(root, 820, 400);
                assertTrue(feedback.getScrollTop() > 0, "Long feedback must remain scrollable");

                layoutWindow(root, 1120, 800);
                assertEquals(normalHeight,
                        root.lookup("#resultDisplayPlaceholder").getLayoutBounds().getHeight(), 0.01);
                assertEquals(message, feedback.getText());
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
    public void execute_findAndFilter_footerCountMatchesShowingMessage() throws Exception {
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
                Label footer = (Label) window.getRoot().getScene().lookup("#clientCountStatus");

                enterCommand(window, commandBox, "find aMY");
                assertEquals("Showing 1 of 2 clients.", feedback.getText());
                assertEquals("1 client", footer.getText());

                enterCommand(window, commandBox, "find Nobody");
                assertEquals("Showing 0 of 2 clients.", feedback.getText());
                assertEquals("0 clients", footer.getText());

                enterCommand(window, commandBox, "filter t/husband");
                assertEquals("Showing 1 of 2 clients.", feedback.getText());
                assertEquals("1 client", footer.getText());

                enterCommand(window, commandBox, "filter t/friend");
                assertEquals("Showing 2 of 2 clients.", feedback.getText());
                assertEquals("2 clients", footer.getText());
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

    @Test
    public void selection_clickThenViewFilteredIndex_showsSelectedClientAndBorder() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            model.addPerson(CARL);
            MainWindow window = openWindow(model);
            try {
                Region root = (Region) window.getRoot().getScene().getRoot();
                layoutWindow(root, 1000, 700);
                assertEmptySelection(window);
                ListView<?> listView = (ListView<?>) root.lookup("#personListView");
                ListCell<?> secondCell = listView.lookupAll(".list-cell").stream()
                        .map(node -> (ListCell<?>) node)
                        .filter(cell -> !cell.isEmpty() && cell.getIndex() == 1)
                        .findFirst().orElseThrow();
                Point2D point = secondCell.localToScene(10, 10);
                secondCell.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED,
                        point.getX(), point.getY(), point.getX(), point.getY(),
                        MouseButton.PRIMARY, 1, false, false, false, false, true, false, false,
                        false, false, true, new PickResult(secondCell, point.getX(), point.getY())));
                secondCell.fireEvent(new MouseEvent(MouseEvent.MOUSE_RELEASED,
                        point.getX(), point.getY(), point.getX(), point.getY(),
                        MouseButton.PRIMARY, 1, false, false, false, false, false, false, false,
                        false, false, true, new PickResult(secondCell, point.getX(), point.getY())));
                assertSelectedClient(window, BOB);
                root.applyCss();
                Region card = (Region) secondCell.getGraphic();
                assertEquals(Color.web("#5aa2ff"), card.getBorder().getStrokes().get(0).getTopStroke());
                assertEquals(1, card.getBorder().getStrokes().get(0).getWidths().getTop());

                TextField commandBox = (TextField) root.lookup("#commandTextField");
                enterCommand(window, commandBox, "view 3");
                assertSelectedClient(window, CARL);
                enterCommand(window, commandBox, "find Bob Carl");
                assertEquals(List.of(BOB, CARL), model.getFilteredPersonList());
                assertEmptySelection(window);
                enterCommand(window, commandBox, "view 2");
                assertEquals(1, listView.getSelectionModel().getSelectedIndex());
                assertSelectedClient(window, CARL);
                enterCommand(window, commandBox, "view 99");
                assertSelectedClient(window, CARL);
                assertTrue(((TextArea) root.lookup("#resultDisplay")).getStyleClass().contains("error"));
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void selection_mutationsRemainVisible_refreshesUpdatedClientDetails() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder(AMY).withRole(ClientRole.BUYER).build());
            model.addPerson(BOB);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                enterCommand(window, commandBox, "filter t/friend");
                enterCommand(window, commandBox, "view 1");
                enterCommand(window, commandBox, "edit 1 n/Updated Client p/85556666 e/updated@example.net");
                Person updatedClient = model.getFilteredPersonList().get(0);
                assertEquals("Updated Client", updatedClient.getName().fullName);
                assertEquals("85556666", updatedClient.getPhone().value);
                assertEquals("updated@example.net", updatedClient.getEmail().value);
                assertSelectedClient(window, updatedClient);

                enterCommand(window, commandBox, "note 1 First line\\nSecond line");
                assertSelectedClient(window, model.getFilteredPersonList().get(0));
                assertEquals("First line\nSecond line", detailLabel(window, "#note").getText());
                enterCommand(window, commandBox, "note 1 clear");
                assertSelectedClient(window, model.getFilteredPersonList().get(0));
                assertFalse(window.getDetailPane().getRoot().lookup("#noteRow").isVisible());

                enterCommand(window, commandBox, "role 1 both");
                assertSelectedClient(window, model.getFilteredPersonList().get(0));
                assertEquals(ClientRole.BOTH.getDisplayValue(), detailLabel(window, "#role").getText());
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void selection_editLeavesFilter_clearsInsteadOfShowingNextClient() throws Exception {
        assertMutationClearsSelection(new PersonMatchesFilterPredicate(Optional.empty(), Set.of(new Tag("friend"))),
                "edit 1 t/");
    }

    @Test
    public void selection_roleLeavesFilter_clearsInsteadOfShowingNextClient() throws Exception {
        assertMutationClearsSelection(new PersonMatchesFilterPredicate(Optional.of(ClientRole.BUYER), Set.of()),
                "role 1 seller");
    }

    @Test
    public void selection_noteLeavesPredicate_clearsInsteadOfShowingNextClient() throws Exception {
        // Current user-facing filters do not search notes; exercise the general post-mutation contract.
        assertMutationClearsSelection(person -> person.getNote() != null, "note 1 clear");
    }

    @Test
    public void selection_clearWarningThenConfirm_preservesSelectionUntilDeletion() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                enterCommand(window, commandBox, "view 1");
                assertSelectedClient(window, AMY);

                enterCommand(window, commandBox, "clear");
                assertEquals(List.of(AMY, BOB), model.getAddressBook().getPersonList());
                assertSelectedClient(window, AMY);
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                assertEquals(String.format(ClearCommand.MESSAGE_WARNING_MULTIPLE_CLIENTS, 2), feedback.getText());

                enterCommand(window, commandBox, "clear confirm");
                assertEquals(ClearCommand.MESSAGE_SUCCESS, feedback.getText());
                assertTrue(model.getAddressBook().getPersonList().isEmpty());
                assertEmptySelection(window);
            } finally {
                window.getRoot().close();
            }
        });
    }

    @Test
    public void selection_help_preservesSelectedClient() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(AMY);
            MainWindow window = openWindow(model);
            TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
            try {
                enterCommand(window, commandBox, "view 1");
                assertSelectedClient(window, AMY);

                enterCommand(window, commandBox, "help");
                TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                assertFalse(feedback.getStyleClass().contains("error"));
                assertSelectedClient(window, AMY);
            } finally {
                // Close both the main window and the help window opened by the command.
                enterCommand(window, commandBox, "exit");
                window.getRoot().close();
            }
        });
    }

    @Test
    public void selection_commandsWithoutViewIndex_clearPanelEvenIfListIsUnchanged() throws Exception {
        runOnFxThread(() -> {
            for (String command : List.of("filter t/friend", "find Amy Bob", "find Nobody", "list",
                    "delete 1", "delete 2")) {
                ModelManager model = new ModelManager();
                model.addPerson(AMY);
                model.addPerson(BOB);
                MainWindow window = openWindow(model);
                try {
                    TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                    window.getPersonListPanel().selectPerson(Index.fromOneBased(1));
                    window.getRoot().getScene().getRoot().applyCss();
                    assertSelectedClient(window, AMY);
                    enterCommand(window, commandBox, command);
                    TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
                    assertFalse(feedback.getStyleClass().contains("error"), command);
                    assertEmptySelection(window);
                } finally {
                    window.getRoot().close();
                }
            }
        });
    }

    private void assertMutationClearsSelection(Predicate<Person> predicate, String command) throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person firstClient = new PersonBuilder(AMY).withRole(ClientRole.BUYER).withNote("First note").build();
            Person nextClient = new PersonBuilder(BOB).withRole(ClientRole.BUYER).withNote("Second note").build();
            model.addPerson(firstClient);
            model.addPerson(nextClient);
            model.updateFilteredPersonList(predicate);
            MainWindow window = openWindow(model);
            try {
                TextField commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
                enterCommand(window, commandBox, "view 1");
                assertSelectedClient(window, firstClient);
                enterCommand(window, commandBox, command);
                assertEquals(List.of(nextClient), model.getFilteredPersonList());
                assertEmptySelection(window);
            } finally {
                window.getRoot().close();
            }
        });
    }

    private static Label detailLabel(MainWindow window, String selector) {
        return (Label) window.getDetailPane().getRoot().lookup(selector);
    }

    private static void assertSelectedClient(MainWindow window, Person person) {
        assertSame(person, window.getPersonListPanel().selectedPersonProperty().get());
        assertEquals(person.getName().fullName, detailLabel(window, "#name").getText());
        assertEquals(person.getPhone().value, detailLabel(window, "#phone").getText());
        assertEquals(person.getEmail().value, detailLabel(window, "#email").getText());
        assertEquals(person.getAddress().value, detailLabel(window, "#address").getText());
        assertFalse(window.getDetailPane().getRoot().lookup("#emptyState").isVisible());
        assertTrue(window.getDetailPane().getRoot().lookup("#detailScroll").isVisible());
    }

    private static void assertEmptySelection(MainWindow window) {
        assertNull(window.getPersonListPanel().selectedPersonProperty().get());
        assertTrue(window.getDetailPane().getRoot().lookup("#emptyState").isVisible());
        assertFalse(window.getDetailPane().getRoot().lookup("#detailScroll").isVisible());
        assertEquals("", detailLabel(window, "#name").getText());
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

    private static void assertWithinRegion(Node child, Node parent) {
        Bounds childBounds = child.localToScene(child.getLayoutBounds());
        Bounds parentBounds = parent.localToScene(parent.getLayoutBounds());
        assertTrue(childBounds.getMinX() >= parentBounds.getMinX() - 1
                && childBounds.getMaxX() <= parentBounds.getMaxX() + 1
                && childBounds.getMinY() >= parentBounds.getMinY() - 1
                && childBounds.getMaxY() <= parentBounds.getMaxY() + 1,
                child.getId() + " extends beyond " + parent.getId());
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

    private static void fireShortcut(TextField commandBox, KeyCode key, boolean shift,
            boolean control, boolean meta) {
        commandBox.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, shift, control, false, meta));
    }

    private static class RecordingLogic implements Logic {
        private final List<String> executedCommands = new ArrayList<>();
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private GuiSettings guiSettings = new GuiSettings();

        @Override
        public CommandResult execute(String commandText) {
            executedCommands.add(commandText);
            return new CommandResult("Executed " + commandText);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public boolean canRedo() {
            return true;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return persons;
        }

        @Override
        public GuiSettings getGuiSettings() {
            return guiSettings;
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            this.guiSettings = guiSettings;
        }
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
