package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.index.Index;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests placeholder visibility and selection in the {@code PersonListPanel}.
 */
public class PersonListPanelTest {

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
            // JavaFX runtime already initialized
            Platform.runLater(initialise);
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void placeholder_emptyListThenAddThenClear_updatesVisibility() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList();
            PersonListPanel panel = new PersonListPanel(persons);
            Region root = panel.getRoot();
            new Scene(root, 600, 400);
            root.resize(600, 400);

            ListView<?> listView = (ListView<?>) root.lookup("#personListView");
            Label placeholder = (Label) listView.getPlaceholder();

            assertEquals("No clients found", placeholder.getText());
            assertTrue(placeholder.getStyleClass().contains("label-bright"));
            assertTrue(isPlaceholderVisible(root));

            persons.add(AMY);
            assertEquals(1, listView.getItems().size());
            assertFalse(isPlaceholderVisible(root));

            persons.clear();
            assertTrue(listView.getItems().isEmpty());
            assertTrue(isPlaceholderVisible(root));
        });
    }

    @Test
    public void selectPerson_thenClearOrUseStaleIndex_updatesSelectedClient() throws Exception {
        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(AMY, BOB));
            assertNull(panel.selectedPersonProperty().get());

            panel.selectPerson(Index.fromOneBased(2));
            assertEquals(BOB, panel.selectedPersonProperty().get());

            panel.clearSelection();
            assertNull(panel.selectedPersonProperty().get());

            panel.selectPerson(Index.fromOneBased(1));
            panel.selectPerson(Index.fromOneBased(3));
            assertNull(panel.selectedPersonProperty().get());
        });
    }

    @Test
    public void selection_listReplacedOrClientDeleted_clearsInsteadOfSelectingAnotherClient() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList(AMY, BOB);
            PersonListPanel panel = new PersonListPanel(persons);
            Region root = panel.getRoot();
            new Scene(root, 600, 400);
            root.applyCss();
            root.layout();
            panel.selectPerson(Index.fromOneBased(1));

            persons.set(0, new PersonBuilder(AMY).withName("Updated Amy").build());
            assertNull(panel.selectedPersonProperty().get());

            panel.selectPerson(Index.fromOneBased(1));
            persons.remove(0);
            assertEquals(List.of(BOB), persons);
            assertNull(panel.selectedPersonProperty().get());
        });
    }

    private static boolean isPlaceholderVisible(Region root) {
        root.applyCss();
        root.layout();
        Node placeholderContainer = root.lookup(".placeholder");
        return placeholderContainer != null && placeholderContainer.isVisible();
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
