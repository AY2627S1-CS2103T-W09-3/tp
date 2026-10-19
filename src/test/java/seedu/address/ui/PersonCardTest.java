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
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
            FlowPane tags = (FlowPane) root.lookup("#tags");

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
            assertEquals(List.of("firsttime", "urgent"), tags.getChildren().stream()
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
