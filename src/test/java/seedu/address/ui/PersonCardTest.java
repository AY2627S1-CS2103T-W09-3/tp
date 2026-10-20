package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void setUpJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
    }

    @Test
    public void constructor_rolePresent_displaysRoleBadge() throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRole(ClientRole.BOTH).withTags("friend").build();

        Label roleBadge = createPersonCard(person);

        assertEquals(ClientRole.BOTH.getDisplayValue(), roleBadge.getText());
        assertTrue(roleBadge.getStyleClass().contains("role-both"));
        assertTrue(roleBadge.isVisible());
        assertTrue(roleBadge.isManaged());
    }

    @Test
    public void constructor_roleAbsent_hidesAndUnmanagesRoleBadge()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().build();

        Label roleBadge = createPersonCard(person);

        assertFalse(roleBadge.isVisible());
        assertFalse(roleBadge.isManaged());
    }

    @Test
    public void constructor_clientDetails_displaysIdentityContactAndTagsWithoutAddress()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRole(ClientRole.BUYER).withTags("urgent", "firsttime").build();
        FutureTask<Void> checkCard = new FutureTask<>(() -> {
            Region root = new PersonCard(person, 2).getRoot();
            VBox content = (VBox) root.lookup("#cardContent");
            HBox identity = (HBox) root.lookup("#identityRow");
            HBox contact = (HBox) root.lookup("#contactRow");
            StackPane tags = (StackPane) root.lookup("#tags");
            HBox tagRow = (HBox) tags.lookup("#tagRow");

            assertEquals(List.of(identity, contact, tags), content.getChildren());
            assertEquals(List.of(root.lookup("#id"), root.lookup("#name"), root.lookup("#role")),
                    identity.getChildren());
            assertEquals("2. ", ((Label) root.lookup("#id")).getText());
            assertEquals(person.getName().fullName, ((Label) root.lookup("#name")).getText());
            assertEquals(List.of(root.lookup("#phone"), root.lookup("#contactSeparator"), root.lookup("#email")),
                    contact.getChildren());
            assertEquals(person.getPhone().value, ((Label) root.lookup("#phone")).getText());
            assertEquals("·", ((Label) root.lookup("#contactSeparator")).getText());
            assertEquals(person.getEmail().value, ((Label) root.lookup("#email")).getText());
            assertEquals(List.of("firsttime", "urgent"), tagRow.getChildren().stream()
                    .filter(tag -> !"tagOverflow".equals(tag.getId()))
                    .map(tag -> ((Label) tag).getText()).toList());
            assertNull(root.lookup("#address"));
            return null;
        });
        Platform.runLater(checkCard);
        checkCard.get();
    }

    @Test
    public void layout_narrowList_keepsBadgeVisibleAndTruncatesLongText()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                .withEmail("alexandra.elizabeth.montgomery@example.com").withRole(ClientRole.BOTH).build();
        FutureTask<Void> checkCard = new FutureTask<>(() -> {
            Region root = new PersonListPanel(FXCollections.observableArrayList(person)).getRoot();
            Scene scene = new Scene(root, 318, 200);
            scene.getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.resize(318, 200);
            root.applyCss();
            root.layout();

            Region card = (Region) root.lookup("#cardPane");
            Label name = (Label) card.lookup("#name");
            Label email = (Label) card.lookup("#email");
            Label badge = (Label) card.lookup("#role");
            Bounds badgeBounds = badge.localToScene(badge.getLayoutBounds());
            assertTrue(card.getWidth() <= 318);
            assertTrue(badgeBounds.getMaxX() <= 318);
            assertTrue(badge.getWidth() >= badge.prefWidth(-1) - 1);
            assertEquals(ClientRole.BOTH.getDisplayValue(), badge.getText());
            assertTrue(name.localToScene(name.getLayoutBounds()).getMaxX() <= badgeBounds.getMinX());
            for (Label text : List.of(name, email)) {
                assertTrue(text.getWidth() < text.prefWidth(-1));
                assertEquals(OverrunStyle.ELLIPSIS, text.getTextOverrun());
                assertFalse(text.isWrapText());
            }
            assertEquals(person.getName().fullName, name.getText());
            assertEquals(person.getEmail().value, email.getText());
            return null;
        });
        Platform.runLater(checkCard);
        checkCard.get();
    }

    @Test
    public void layout_optionalFields_keepsEveryCardAtSeventyPixels()
            throws ExecutionException, InterruptedException {
        List<Person> clients = List.of(
                new PersonBuilder().withName("Alicia Tan").withRole(ClientRole.BUYER).withTags("firsttime").build(),
                new PersonBuilder().withName("Priya Nair").withRole(ClientRole.SELLER).withTags().build(),
                new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                        .withEmail("alexandra.elizabeth.montgomery@example.com").withRole(ClientRole.BOTH)
                        .withTags("urgent").build(),
                new PersonBuilder().withName("Ravi Kumar").withTags().build());
        FutureTask<Void> checkCards = new FutureTask<>(() -> {
            Region root = new PersonListPanel(FXCollections.observableArrayList(clients)).getRoot();
            Scene scene = new Scene(root, 318, 320);
            scene.getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.resize(318, 320);
            root.applyCss();
            root.layout();

            ListView<?> personList = (ListView<?>) root.lookup("#personListView");
            personList.getSelectionModel().select(0);
            root.applyCss();
            root.layout();

            List<Region> cards = root.lookupAll("#cardPane").stream().map(node -> (Region) node).toList();
            assertEquals(clients.size(), cards.size());
            for (Region card : cards) {
                assertEquals(70, card.getHeight(), 0.01);
                assertEquals(3, ((VBox) card.lookup("#cardContent")).getChildren().size());
                Bounds cardBounds = card.localToScene(card.getLayoutBounds());
                for (String id : List.of("identityRow", "contactRow", "tags")) {
                    Bounds bounds = card.lookup("#" + id).localToScene(card.lookup("#" + id).getLayoutBounds());
                    assertTrue(bounds.getMinY() >= cardBounds.getMinY());
                    assertTrue(bounds.getMaxY() <= cardBounds.getMaxY());
                }
            }
            return null;
        });
        Platform.runLater(checkCards);
        checkCards.get();
    }

    @Test
    public void layout_compactViewport_showsThreeCompleteCards()
            throws ExecutionException, InterruptedException {
        List<Person> clients = List.of(
                new PersonBuilder().withName("Alicia Tan").withRole(ClientRole.BUYER).withTags("firsttime").build(),
                new PersonBuilder().withName("Benjamin Lim").withRole(ClientRole.BUYER).withTags("urgent").build(),
                new PersonBuilder().withName("Priya Nair").withRole(ClientRole.SELLER).withTags().build(),
                new PersonBuilder().withName("Ravi Kumar").withRole(ClientRole.BOTH).withTags("upgrader").build());
        FutureTask<Void> checkCards = new FutureTask<>(() -> {
            // A conservative list-only viewport within the smallest planned window layout.
            Region root = new PersonListPanel(FXCollections.observableArrayList(clients)).getRoot();
            Scene scene = new Scene(root, 318, 220);
            scene.getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.resize(318, 220);
            root.applyCss();
            root.layout();

            List<ListCell<?>> firstThree = root.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getIndex() < 3)
                    .<ListCell<?>>map(node -> (ListCell<?>) node).toList();
            assertEquals(3, firstThree.size());
            for (ListCell<?> cell : firstThree) {
                Bounds bounds = cell.localToScene(cell.getLayoutBounds());
                assertTrue(bounds.getMinY() >= 0);
                assertTrue(bounds.getMaxY() <= 220);
                assertEquals(70, cell.getGraphic().getLayoutBounds().getHeight(), 0.01);
            }
            return null;
        });
        Platform.runLater(checkCards);
        checkCards.get();
    }

    @Test
    public void layout_tagOverflow_showsCountAndRestoresTagsWhenWidened()
            throws ExecutionException, InterruptedException {
        List<String> tagNames = List.of("buyer", "firsttime", "investment", "nearMrt", "urgent");
        Person person = new PersonBuilder().withTags(tagNames.toArray(String[]::new)).build();
        FutureTask<Void> checkCard = new FutureTask<>(() -> {
            Region root = new PersonCard(person, 1).getRoot();
            Scene scene = new Scene(root, 180, 70);
            scene.getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.resize(180, 70);
            root.applyCss();
            root.layout();

            HBox tagRow = (HBox) root.lookup("#tagRow");
            Label overflow = (Label) root.lookup("#tagOverflow");
            // Repeating layout must settle instead of continuously invalidating the row.
            root.layout();
            root.layout();
            assertFalse(tagRow.isNeedsLayout());
            List<Label> visibleTags = tagRow.getChildren().stream().filter(node -> node != overflow && node.isVisible())
                    .map(node -> (Label) node).toList();
            int hidden = tagNames.size() - visibleTags.size();
            assertTrue(hidden > 0);
            assertTrue(overflow.isVisible());
            assertEquals("+" + hidden, overflow.getText());
            assertEquals("Additional tags: " + String.join(", ", tagNames.subList(visibleTags.size(), tagNames.size())),
                    overflow.getTooltip().getText());
            Bounds tagBounds = tagRow.localToScene(tagRow.getLayoutBounds());
            for (javafx.scene.Node tag : tagRow.getChildren()) {
                if (tag.isVisible()) {
                    Bounds bounds = tag.localToScene(tag.getLayoutBounds());
                    assertTrue(bounds.getMaxX() <= tagBounds.getMaxX() + 1);
                    assertEquals(tagBounds.getMinY(), bounds.getMinY(), 1);
                }
            }

            root.resize(700, 70);
            root.layout();
            assertFalse(overflow.isVisible());
            assertFalse(overflow.isManaged());
            assertEquals(tagNames, tagRow.getChildren().stream().filter(node -> node != overflow && node.isVisible())
                    .map(node -> ((Label) node).getText()).toList());
            assertEquals(70, root.getHeight(), 0.01);
            return null;
        });
        Platform.runLater(checkCard);
        checkCard.get();
    }

    @Test
    public void layout_singleOversizedTag_keepsOneRowAndExposesFullTagInTooltip()
            throws ExecutionException, InterruptedException {
        String tagName = "averylongtagthatcannotfitwithinthenarrowcard";
        Person person = new PersonBuilder().withTags(tagName).build();
        FutureTask<Void> checkCard = new FutureTask<>(() -> {
            Region root = new PersonCard(person, 1).getRoot();
            Scene scene = new Scene(root, 180, 70);
            scene.getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.resize(180, 70);
            root.applyCss();
            root.layout();

            Label overflow = (Label) root.lookup("#tagOverflow");
            assertTrue(overflow.isVisible());
            assertEquals("+1", overflow.getText());
            assertEquals("Additional tags: " + tagName, overflow.getTooltip().getText());
            assertEquals(70, root.getHeight(), 0.01);
            assertEquals(16, ((StackPane) root.lookup("#tags")).getHeight(), 0.01);
            return null;
        });
        Platform.runLater(checkCard);
        checkCard.get();
    }

    @Test
    public void getRoleStyleClass_eachRole_returnsColourClass() {
        assertEquals("role-buyer", PersonCard.getRoleStyleClass(ClientRole.BUYER));
        assertEquals("role-seller", PersonCard.getRoleStyleClass(ClientRole.SELLER));
        assertEquals("role-both", PersonCard.getRoleStyleClass(ClientRole.BOTH));
    }

    private static Label createPersonCard(Person person)
            throws ExecutionException, InterruptedException {
        FutureTask<Label> createCard = new FutureTask<>(() -> {
            PersonCard personCard = new PersonCard(person, 1);
            return (Label) personCard.getRoot().lookup("#role");
        });
        Platform.runLater(createCard);
        return createCard.get();
    }
}
