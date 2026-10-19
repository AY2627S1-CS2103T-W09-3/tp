package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the standalone detail panel before the model and main-window dependencies are integrated.
 */
public class DetailPaneTest {
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
    public void constructor_noSelection_showsEmptyState() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            layout(pane, 380, 280);

            assertShown(pane.getRoot().lookup("#emptyState"), true);
            assertEquals("Select a client to see details.", label(pane, "emptyState").getText());
            assertShown(pane.getRoot().lookup("#detailScroll"), false);
        });
    }

    @Test
    public void setPerson_basicClient_showsOnlyFourRows() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            Person person = new PersonBuilder().build();
            pane.setPerson(person);
            layout(pane, 380, 400);

            assertShown(pane.getRoot().lookup("#emptyState"), false);
            assertShown(pane.getRoot().lookup("#detailScroll"), true);
            assertEquals(person.getName().fullName, label(pane, "name").getText());
            assertEquals(person.getPhone().value, label(pane, "phone").getText());
            assertEquals(person.getEmail().value, label(pane, "email").getText());
            assertEquals(person.getAddress().value, label(pane, "address").getText());
            assertShown(label(pane, "role"), false);
            assertEquals(List.of("nameRow", "phoneRow", "emailRow", "addressRow"), visibleRows(pane));
        });
    }

    @Test
    public void setPerson_roleChanges_replacesBadgeStyle() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            pane.setPerson(new PersonBuilder().withRole(ClientRole.BUYER).build());
            layout(pane, 380, 400);
            Label badge = label(pane, "role");
            assertEquals("Buyer", badge.getText());
            assertTrue(badge.getStyleClass().contains("role-buyer"));

            pane.setPerson(new PersonBuilder().withRole(ClientRole.SELLER).build());
            assertEquals("Seller", badge.getText());
            assertTrue(badge.getStyleClass().contains("role-seller"));
            assertFalse(badge.getStyleClass().contains("role-buyer"));

            pane.setPerson(new PersonBuilder().withRole(ClientRole.BOTH).build());
            assertEquals(ClientRole.BOTH.getDisplayValue(), badge.getText());
            assertTrue(badge.getStyleClass().contains("role-both"));
            assertFalse(badge.getStyleClass().contains("role-seller"));
            assertShown(badge, true);

            pane.setPerson(new PersonBuilder().build());
            assertShown(badge, false);
            assertFalse(badge.getStyleClass().contains("role-both"));
        });
    }

    @Test
    public void setAdditionalDetails_previewValues_showsRowsInOrder() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            pane.setPerson(new PersonBuilder().withTags("urgent", "buyer").build());
            pane.setAdditionalDetails("$800,000 – $1,200,000", "$950,000", "Bishan, Toa Payoh",
                    "HDB, Condo", "Call after work.\nPrefers weekend viewings.");
            layout(pane, 380, 400);

            assertEquals(List.of("nameRow", "phoneRow", "emailRow", "addressRow", "budgetRow", "askingPriceRow",
                    "preferredAreasRow", "housingTypesRow", "noteRow", "tagsRow"), visibleRows(pane));
            assertEquals("$800,000 – $1,200,000", label(pane, "budget").getText());
            assertEquals("$950,000", label(pane, "askingPrice").getText());
            assertEquals("Bishan, Toa Payoh", label(pane, "preferredAreas").getText());
            assertEquals("HDB, Condo", label(pane, "housingTypes").getText());
            assertEquals("Call after work.\nPrefers weekend viewings.", label(pane, "note").getText());
            FlowPane tags = (FlowPane) pane.getRoot().lookup("#tags");
            assertEquals(List.of("buyer", "urgent"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
        });
    }

    @Test
    public void setAdditionalDetails_missingValues_hidesEntireRows() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            pane.setPerson(new PersonBuilder().build());
            pane.setAdditionalDetails(null, "", "   ", "HDB", "\n ");
            layout(pane, 380, 400);

            assertEquals(List.of("nameRow", "phoneRow", "emailRow", "addressRow", "housingTypesRow"),
                    visibleRows(pane));
            for (String row : List.of("budgetRow", "askingPriceRow", "preferredAreasRow", "noteRow", "tagsRow")) {
                assertShown(pane.getRoot().lookup("#" + row), false);
            }
        });
    }

    @Test
    public void setPerson_switchAndClear_removesPreviousDetailsAndResetsScroll() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            pane.setPerson(new PersonBuilder().withRole(ClientRole.BUYER).withTags("urgent").build());
            pane.setAdditionalDetails("$800,000", "$950,000", "Bishan", "HDB", "Private note.\n".repeat(30));
            layout(pane, 380, 280);
            ScrollPane scroll = (ScrollPane) pane.getRoot().lookup("#detailScroll");
            scroll.setVvalue(1);

            pane.setPerson(new PersonBuilder().withName("Ben Lim").withPhone("81112222")
                    .withEmail("ben@example.com").withAddress("2 Test Road").build());
            layout(pane, 380, 280);
            assertEquals("Ben Lim", label(pane, "name").getText());
            assertEquals("81112222", label(pane, "phone").getText());
            assertEquals("ben@example.com", label(pane, "email").getText());
            assertEquals("2 Test Road", label(pane, "address").getText());
            assertEquals(0, scroll.getVvalue());
            assertEquals(List.of("nameRow", "phoneRow", "emailRow", "addressRow"), visibleRows(pane));
            assertTrue(((FlowPane) pane.getRoot().lookup("#tags")).getChildren().isEmpty());
            for (String value : List.of("budget", "askingPrice", "preferredAreas", "housingTypes", "note", "role")) {
                assertEquals("", label(pane, value).getText());
            }

            pane.setPerson(null);
            assertShown(scroll, false);
            assertShown(pane.getRoot().lookup("#emptyState"), true);
            assertEquals("", label(pane, "name").getText());
        });
    }

    @Test
    public void layout_longValuesInNarrowPanel_wrapsAndScrollsToLastRow() throws Exception {
        assertLongValuesWrapAndScrollToLastRow("");
    }

    @Test
    public void layout_largeTagFont_wrapsAndScrollsToLastRow() throws Exception {
        assertLongValuesWrapAndScrollToLastRow("-fx-font-family: 'Arial'; -fx-font-size: 20px;");
    }

    private static void assertLongValuesWrapAndScrollToLastRow(String tagStyle) throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            Person person = new PersonBuilder().withName("A very long client name ".repeat(5).trim())
                    .withRole(ClientRole.BOTH).withEmail("longemail".repeat(10) + "@example.com")
                    .withAddress("A long address with building and unit details ".repeat(8).trim())
                    .withTags("urgent", "longtag".repeat(30)).build();
            String noteText = "First line ".repeat(15) + "\n" + "Second line ".repeat(14) + "\n"
                    + "Third line ".repeat(15);
            assertEquals(500, noteText.length());
            pane.setPerson(person);
            pane.setAdditionalDetails("$800,000 – $1,200,000", "$950,000", "Bishan, Toa Payoh",
                    "HDB, Condo", noteText);
            // A 380-wide detail pane must fit within the 853 x 480 logical-pixel target window.
            layout(pane, 380, 280);
            FlowPane tags = (FlowPane) pane.getRoot().lookup("#tags");
            tags.getChildren().forEach(tag -> tag.setStyle(tagStyle));
            layout(pane, 380, 280);

            ScrollPane scroll = (ScrollPane) pane.getRoot().lookup("#detailScroll");
            VBox rows = (VBox) scroll.getContent();
            assertTrue(rows.getHeight() > scroll.getViewportBounds().getHeight());
            assertTrue(rows.getWidth() <= scroll.getViewportBounds().getWidth() + 1);
            assertEquals(noteText, label(pane, "note").getText());
            for (String id : List.of("name", "email", "address", "note")) {
                Label value = label(pane, id);
                assertTrue(value.isWrapText());
                assertTrue(value.getHeight() >= value.prefHeight(value.getWidth()) - 1, id + " is clipped");
                assertTrue(value.getHeight() > 25, id + " did not wrap");
            }
            for (Node tag : tags.getChildren()) {
                assertTrue(tag.getBoundsInParent().getMaxY() <= tags.getHeight() + 1,
                        "A wrapped tag exceeds its row height");
            }
            Node viewport = scroll.lookup(".viewport");
            Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
            for (Node node : pane.getRoot().lookupAll(".label")) {
                Bounds bounds = node.localToScene(node.getBoundsInLocal());
                assertTrue(bounds.getMaxX() <= viewportBounds.getMaxX() + 1, "Text exceeds the panel width");
            }

            scroll.setVvalue(1);
            layout(pane, 380, 280);
            Node tagsRow = pane.getRoot().lookup("#tagsRow");
            Bounds lastRow = tagsRow.localToScene(tagsRow.getBoundsInLocal());
            assertTrue(lastRow.getMaxY() <= viewportBounds.getMaxY() + 1, "The final row is unreachable");
            assertTrue(lastRow.getMaxY() > viewportBounds.getMinY());
        });
    }

    private static Label label(DetailPane pane, String id) {
        return (Label) pane.getRoot().lookup("#" + id);
    }

    private static List<String> visibleRows(DetailPane pane) {
        VBox rows = (VBox) pane.getRoot().lookup("#detailRows");
        return rows.getChildren().stream().filter(Node::isManaged).map(Node::getId).toList();
    }

    private static void assertShown(Node node, boolean shown) {
        assertEquals(shown, node.isVisible());
        assertEquals(shown, node.isManaged());
    }

    private static void layout(DetailPane pane, double width, double height) {
        if (pane.getRoot().getScene() == null) {
            new Scene(pane.getRoot(), width, height);
        }
        pane.getRoot().resize(width, height);
        pane.getRoot().applyCss();
        // Wrapping and the scrollbar can change the available width and request another layout pass.
        for (int pass = 0; pass < 10 && pane.getRoot().isNeedsLayout(); pass++) {
            pane.getRoot().layout();
        }
        assertFalse(pane.getRoot().isNeedsLayout(), "The detail panel layout did not settle");
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
