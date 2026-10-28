package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.PersonCardTestUtil.assertCardContentFits;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks the rendered theme rather than relying on style-class names alone.
 */
public class InterfaceStyleTest {
    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
    }

    @Test
    public void style_selectingCards_preservesDistinctReadableRoleBadges() throws Exception {
        runOnFxThread(() -> {
            List<Person> clients = List.of(
                    new PersonBuilder().withName("Alicia Tan").withRole(ClientRole.BUYER).withTags("firsttime").build(),
                    new PersonBuilder().withName("Priya Nair").withRole(ClientRole.SELLER).withTags("urgent").build(),
                    new PersonBuilder().withName("Cheryl Goh").withRole(ClientRole.BOTH).withTags("upgrading").build());
            Region root = new PersonListPanel(FXCollections.observableArrayList(clients)).getRoot();
            addStylesheets(root, 318, 320);
            ListView<?> list = (ListView<?>) root.lookup("#personListView");
            Set<Color> roleColours = new HashSet<>();
            for (int index = 0; index < clients.size(); index++) {
                roleColours.add(assertSelectedRoleBadge(root, list, clients.get(index), index));
            }
            assertEquals(3, roleColours.size());
        });
    }

    private static Color assertSelectedRoleBadge(Region root, ListView<?> list, Person client, int index) {
        list.getSelectionModel().clearSelection();
        settle(root);
        Region card = cardAt(list, index);
        Label badge = (Label) card.lookup("#role");
        Color roleColour = background(badge);
        Color unselectedColour = background(card);
        double originalHeight = card.getHeight();

        list.getSelectionModel().select(index);
        settle(root);
        card = cardAt(list, index);
        badge = (Label) card.lookup("#role");
        assertNotEquals(unselectedColour, background(card));
        assertEquals(originalHeight, card.getHeight(), 0.01);
        assertEquals(roleColour, background(badge));
        assertEquals(client.getRole().getDisplayValue(), badge.getText());
        assertTrue(badge.isVisible() && badge.isManaged());
        assertTrue(badge.getWidth() >= badge.prefWidth(-1) - 1);
        assertReadable(badge, roleColour);
        assertReadable((Label) card.lookup("#name"), background(card));
        assertReadable((Label) card.lookup("#email"), background(card));
        assertTrue(card.getBorder().getStrokes().getFirst().getTopStroke() instanceof Color);
        assertNotEquals(Color.TRANSPARENT, card.getBorder().getStrokes().getFirst().getTopStroke());
        assertCardContentFits(card);
        return roleColour;
    }

    @Test
    public void style_cardAndProfileTags_shareReadablePillAppearance() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withRole(ClientRole.BOTH).withTags("urgent").build();
            Region card = new PersonCard(person, 1).getRoot();
            addStylesheets(card, 318, 90);
            Label cardTag = (Label) card.lookup("#tags").lookup(".label");
            assertReadable(cardTag, background(cardTag));
            assertTrue(cardTag.getBackground().getFills().getFirst().getRadii().getTopLeftHorizontalRadius() > 0);

            DetailPane panel = new DetailPane();
            panel.setPerson(person);
            Region profile = panel.getRoot();
            addStylesheets(profile, 400, 600);
            Label profileTag = (Label) profile.lookup("#tags").lookup(".label");
            assertEquals(background(cardTag), background(profileTag));
            assertEquals(cardTag.getTextFill(), profileTag.getTextFill());
            assertReadable(profileTag, background(profileTag));
            assertFalse(((Label) profile.lookup("#role")).getText().contains("+"));
        });
    }

    @Test
    public void style_profileCaptionAndEmptyState_areReadableOnThePanel() throws Exception {
        runOnFxThread(() -> {
            DetailPane panel = new DetailPane();
            Region root = panel.getRoot();
            addStylesheets(root, 400, 600);
            Label emptyState = (Label) root.lookup("#emptyState");
            assertTrue(emptyState.isVisible());
            assertReadable(emptyState, background(root));

            panel.setPerson(new PersonBuilder().withTags("urgent").build());
            settle(root);
            assertFalse(emptyState.isManaged());
            for (var node : root.lookupAll(".detail-caption")) {
                assertReadable((Label) node, background(root));
            }
            for (String id : List.of("name", "phone", "email", "address")) {
                assertReadable((Label) root.lookup("#" + id), background(root));
            }
        });
    }

    private static void addStylesheets(Region root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        for (String stylesheet : List.of("DarkTheme.css", "Extensions.css")) {
            scene.getStylesheets().add(InterfaceStyleTest.class.getResource("/view/" + stylesheet).toExternalForm());
        }
        root.resize(width, height);
        settle(root);
    }

    private static Region cardAt(ListView<?> list, int index) {
        return list.lookupAll(".list-cell").stream()
                .filter(node -> node instanceof ListCell<?> cell && !cell.isEmpty() && cell.getIndex() == index)
                .map(node -> (Region) ((ListCell<?>) node).getGraphic()).findFirst().orElseThrow();
    }

    private static Color background(Region region) {
        return (Color) region.getBackground().getFills().getFirst().getFill();
    }

    private static void assertReadable(Label label, Color background) {
        Color foreground = (Color) label.getTextFill();
        double foregroundLight = luminance(foreground);
        double backgroundLight = luminance(background);
        double contrast = (Math.max(foregroundLight, backgroundLight) + 0.05)
                / (Math.min(foregroundLight, backgroundLight) + 0.05);
        assertTrue(contrast >= 4.5, label.getText() + " has insufficient contrast: " + contrast);
    }

    private static double luminance(Color colour) {
        return 0.2126 * linearChannel(colour.getRed()) + 0.7152 * linearChannel(colour.getGreen())
                + 0.0722 * linearChannel(colour.getBlue());
    }

    private static double linearChannel(double channel) {
        return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    private static void settle(Region root) {
        for (int pass = 0; pass < 4; pass++) {
            root.applyCss();
            root.layout();
        }
    }

    private static void runOnFxThread(Runnable task) throws Exception {
        FutureTask<Void> action = new FutureTask<>(task, null);
        Platform.runLater(action);
        action.get(10, TimeUnit.SECONDS);
    }
}
