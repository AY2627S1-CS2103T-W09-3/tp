package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
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
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Person;
import seedu.address.model.person.Price;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests client data, empty states and wrapping in the detail panel.
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
    public void setPerson_allFields_showsFormattedRowsInOrder() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            pane.setPerson(new PersonBuilder().withRole(ClientRole.BOTH).withTags("urgent", "buyer")
                    .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                    .withAskingPrice(new Price("950000"))
                    .withAreas(Set.of(new Area("Toa Payoh"), new Area("Bishan")))
                    .withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO))
                    .withNote("Call after work.\nPrefers weekend viewings.").build());
            layout(pane, 380, 400);

            assertEquals(List.of("nameRow", "phoneRow", "emailRow", "addressRow", "budgetRow", "askingPriceRow",
                    "preferredAreasRow", "housingTypesRow", "noteRow", "tagsRow"), visibleRows(pane));
            assertEquals("$800,000 to $1,200,000", label(pane, "budget").getText());
            assertEquals("$950,000", label(pane, "askingPrice").getText());
            assertEquals("Bishan, Toa Payoh", label(pane, "preferredAreas").getText());
            assertEquals("Condo, HDB", label(pane, "housingTypes").getText());
            assertEquals("Call after work.\nPrefers weekend viewings.", label(pane, "note").getText());
            FlowPane tags = (FlowPane) pane.getRoot().lookup("#tags");
            assertEquals(List.of("buyer", "urgent"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
        });
    }

    @Test
    public void setPerson_someOptionalFields_showsOnlyPopulatedRows() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            pane.setPerson(new PersonBuilder().withHousingTypes(Set.of(HousingType.HDB)).build());
            layout(pane, 380, 400);

            assertEquals(List.of("nameRow", "phoneRow", "emailRow", "addressRow", "housingTypesRow"),
                    visibleRows(pane));
            assertEquals("HDB", label(pane, "housingTypes").getText());
            for (String row : List.of("budgetRow", "askingPriceRow", "preferredAreasRow", "noteRow", "tagsRow")) {
                assertShown(pane.getRoot().lookup("#" + row), false);
            }
        });
    }

    @Test
    public void setPerson_switchAndClear_removesPreviousDetailsAndResetsScroll() throws Exception {
        runOnFxThread(() -> {
            DetailPane pane = new DetailPane();
            Person fullClient = new PersonBuilder().withRole(ClientRole.BUYER).withTags("urgent")
                    .withBudget(new Budget(new Price("800000"), new Price("800000")))
                    .withAskingPrice(new Price("950000"))
                    .withAreas(Set.of(new Area("Bishan"))).withHousingTypes(Set.of(HousingType.HDB))
                    .withNote("Private note.\n".repeat(30)).build();
            pane.setPerson(fullClient);
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

            pane.setPerson(fullClient);
            layout(pane, 380, 280);
            scroll.setVvalue(1);
            pane.setPerson(null);
            assertShown(scroll, false);
            assertShown(pane.getRoot().lookup("#emptyState"), true);
            assertEquals(0, scroll.getVvalue());
            assertTrue(((FlowPane) pane.getRoot().lookup("#tags")).getChildren().isEmpty());
            for (String value : List.of("name", "phone", "email", "address", "role", "budget", "askingPrice",
                    "preferredAreas", "housingTypes", "note")) {
                assertEquals("", label(pane, value).getText());
            }
            for (String row : List.of("budgetRow", "askingPriceRow", "preferredAreasRow",
                    "housingTypesRow", "noteRow", "tagsRow")) {
                assertShown(pane.getRoot().lookup("#" + row), false);
            }
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
            String noteText = "First line ".repeat(15) + "\n" + "Second line ".repeat(14) + "\n"
                    + "Third line ".repeat(15).stripTrailing() + ".";
            assertEquals(500, noteText.length());
            Person person = new PersonBuilder().withName("A very long client name ".repeat(5).trim())
                    .withRole(ClientRole.BOTH).withEmail("longemail".repeat(10) + "@example.com")
                    .withAddress("A long address with building and unit details ".repeat(8).trim())
                    .withTags("urgent", "longtag".repeat(30))
                    .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                    .withAskingPrice(new Price("950000"))
                    .withAreas(Set.of(new Area("Bishan"), new Area("Toa Payoh")))
                    .withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO))
                    .withNote(noteText).build();
            pane.setPerson(person);
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
