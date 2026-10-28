package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.PersonCardTestUtil.assertCardContentFits;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Person;
import seedu.address.model.person.Price;
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
        runOnFxThread(() -> {
            Region root = new PersonCard(person, 2).getRoot();
            VBox content = (VBox) root.lookup("#cardContent");
            HBox identity = (HBox) root.lookup("#identityRow");
            HBox contact = (HBox) root.lookup("#contactRow");
            StackPane tags = (StackPane) root.lookup("#tags");
            HBox tagRow = (HBox) tags.lookup("#tagRow");

            assertEquals(List.of(identity, contact, tags), content.getChildren().stream()
                    .filter(javafx.scene.Node::isManaged).toList());
            assertEquals(List.of(root.lookup("#id"), root.lookup("#name"), root.lookup("#noteIndicator"),
                    root.lookup("#role")),
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
        });
    }

    @Test
    public void layout_narrowList_keepsBadgeVisibleAndTruncatesLongText()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                .withEmail("alexandra.elizabeth.montgomery@example.com").withRole(ClientRole.BOTH)
                .withNote("Call next week").build();
        runOnFxThread(() -> {
            Region root = new PersonListPanel(FXCollections.observableArrayList(person)).getRoot();
            createStyledScene(root, 318, 200);
            root.resize(318, 200);
            root.applyCss();
            root.layout();

            Region card = (Region) root.lookup("#cardPane");
            Label name = (Label) card.lookup("#name");
            Label email = (Label) card.lookup("#email");
            Label badge = (Label) card.lookup("#role");
            Label marker = (Label) card.lookup("#noteIndicator");
            Bounds markerBounds = marker.localToScene(marker.getLayoutBounds());
            Bounds badgeBounds = badge.localToScene(badge.getLayoutBounds());
            assertTrue(card.getWidth() <= 318);
            assertTrue(badgeBounds.getMaxX() <= 318);
            assertTrue(badge.getWidth() >= badge.prefWidth(-1) - 1);
            assertEquals(ClientRole.BOTH.getDisplayValue(), badge.getText());
            assertTrue(name.localToScene(name.getLayoutBounds()).getMaxX() <= markerBounds.getMinX());
            assertTrue(markerBounds.getMaxX() <= badgeBounds.getMinX());
            assertEquals(20, marker.getWidth(), 0.01);
            assertEquals(marker.getWidth(), marker.getHeight(), 0.01);
            for (Label text : List.of(name, email)) {
                assertTrue(text.getWidth() < text.prefWidth(-1));
                assertEquals(OverrunStyle.ELLIPSIS, text.getTextOverrun());
                assertFalse(text.isWrapText());
            }
            assertEquals(person.getName().fullName, name.getText());
            assertEquals(person.getEmail().value, email.getText());
        });
    }

    @Test
    public void constructor_notePresent_showsAccessibleIconWithoutText()
            throws ExecutionException, InterruptedException {
        runOnFxThread(() -> {
            Region root = new PersonCard(new PersonBuilder().withNote("Call next week").build(), 1).getRoot();
            Label marker = (Label) root.lookup("#noteIndicator");
            assertTrue(marker.isVisible());
            assertTrue(marker.isManaged());
            assertEquals("", marker.getText());
            assertEquals(ContentDisplay.GRAPHIC_ONLY, marker.getContentDisplay());
            assertTrue(marker.getGraphic() instanceof SVGPath);
            assertEquals("Has note", marker.getTooltip().getText());
            assertEquals("Has note", marker.getAccessibleText());
            assertEquals(AccessibleRole.TEXT, marker.getAccessibleRole());
        });
    }

    @Test
    public void constructor_noteAbsent_hidesMarkerWithoutReservingSpace()
            throws ExecutionException, InterruptedException {
        runOnFxThread(() -> {
            Region root = new PersonCard(new PersonBuilder().build(), 1).getRoot();
            Label marker = (Label) root.lookup("#noteIndicator");
            assertFalse(marker.isVisible());
            assertFalse(marker.isManaged());
        });
    }

    @Test
    public void layout_optionalFields_keepsCardsCompactAndTheSameHeight()
            throws ExecutionException, InterruptedException {
        List<Person> clients = List.of(
                new PersonBuilder().withName("Alicia Tan").withRole(ClientRole.BUYER).withTags("firsttime")
                        .withNote("Call next week").build(),
                new PersonBuilder().withName("Priya Nair").withRole(ClientRole.SELLER).withTags().build(),
                new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                        .withEmail("alexandra.elizabeth.montgomery@example.com").withRole(ClientRole.BOTH)
                        .withTags("urgent").build(),
                new PersonBuilder().withName("Ravi Kumar").withTags().build());
        runOnFxThread(() -> {
            Region root = new PersonListPanel(FXCollections.observableArrayList(clients)).getRoot();
            createStyledScene(root, 318, 320);
            root.resize(318, 320);
            root.applyCss();
            root.layout();

            ListView<?> personList = (ListView<?>) root.lookup("#personListView");
            personList.getSelectionModel().select(0);
            root.applyCss();
            root.layout();
            // Selection can recreate cell graphics; apply their CSS before measuring computed heights.
            root.applyCss();
            root.layout();

            List<Region> cards = root.lookupAll("#cardPane").stream().map(node -> (Region) node).toList();
            assertEquals(clients.size(), cards.size());
            double cardHeight = cards.getFirst().getHeight();
            assertTrue(cardHeight >= 70 && cardHeight <= 220.0 / 3,
                    "Default-font cards must stay compact enough for three to fit in the small list");
            for (Region card : cards) {
                assertEquals(cardHeight, card.getHeight(), 0.01);
                assertCardContentFits(card);
                assertEquals(3, ((VBox) card.lookup("#cardContent")).getChildren().stream()
                        .filter(javafx.scene.Node::isManaged).count());
                Bounds cardBounds = card.localToScene(card.getLayoutBounds());
                for (String id : List.of("identityRow", "contactRow", "tags")) {
                    Bounds bounds = card.lookup("#" + id).localToScene(card.lookup("#" + id).getLayoutBounds());
                    assertTrue(bounds.getMinY() >= cardBounds.getMinY());
                    assertTrue(bounds.getMaxY() <= cardBounds.getMaxY());
                }
            }
        });
    }

    @Test
    public void layout_compactViewport_showsThreeCompleteCards()
            throws ExecutionException, InterruptedException {
        List<Person> clients = List.of(
                new PersonBuilder().withName("Alicia Tan").withRole(ClientRole.BUYER).withTags("firsttime").build(),
                new PersonBuilder().withName("Benjamin Lim").withRole(ClientRole.BUYER).withTags("urgent").build(),
                new PersonBuilder().withName("Priya Nair").withRole(ClientRole.SELLER).withTags().build(),
                new PersonBuilder().withName("Ravi Kumar").withRole(ClientRole.BOTH).withTags("upgrader").build());
        runOnFxThread(() -> {
            // A conservative list-only viewport within the smallest planned window layout.
            Region root = new PersonListPanel(FXCollections.observableArrayList(clients)).getRoot();
            createStyledScene(root, 318, 220);
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
                assertCardContentFits((Region) cell.getGraphic());
            }
        });
    }

    @Test
    public void layout_tagOverflow_showsCountAndRestoresTagsWhenWidened()
            throws ExecutionException, InterruptedException {
        List<String> tagNames = List.of("buyer", "firsttime", "investment", "nearMrt", "urgent");
        Person person = new PersonBuilder().withTags(tagNames.toArray(String[]::new)).build();
        runOnFxThread(() -> {
            Region root = new PersonCard(person, 1).getRoot();
            createStyledScene(root, 180, 70);
            layoutContentSizedCard(root, 180);
            double originalHeight = root.getHeight();

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

            layoutContentSizedCard(root, 700);
            assertFalse(overflow.isVisible());
            assertFalse(overflow.isManaged());
            assertEquals(tagNames, tagRow.getChildren().stream().filter(node -> node != overflow && node.isVisible())
                    .map(node -> ((Label) node).getText()).toList());
            assertEquals(originalHeight, root.getHeight(), 0.01);
            assertCardContentFits(root);
        });
    }

    @Test
    public void layout_singleOversizedTag_keepsOneRowAndExposesFullTagInTooltip()
            throws ExecutionException, InterruptedException {
        String tagName = "averylongtagthatcannotfitwithinthenarrowcard";
        Person person = new PersonBuilder().withTags(tagName).build();
        runOnFxThread(() -> {
            Region root = new PersonCard(person, 1).getRoot();
            createStyledScene(root, 180, 70);
            layoutContentSizedCard(root, 180);

            Label overflow = (Label) root.lookup("#tagOverflow");
            assertTrue(overflow.isVisible());
            assertEquals("+1", overflow.getText());
            assertEquals("Additional tags: " + tagName, overflow.getTooltip().getText());
            assertCardContentFits(root);
            assertTrue(((StackPane) root.lookup("#tags")).getHeight() >= overflow.prefHeight(-1) - 1,
                    "The tag row must fit the overflow label's font rather than be capped at 16 pixels");
        });
    }

    @Test
    public void constructor_buyerRequirements_displaysBudgetAreasAndHousingTypes()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRole(ClientRole.BUYER)
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAreas(Set.of(new Area("Tampines"), new Area("Bishan")))
                .withHousingTypes(Set.of(HousingType.HDB)).build();

        assertRequirementsSummary(person, "Budget $800,000 to $1,200,000 · Bishan, Tampines · HDB");
    }

    @Test
    public void constructor_sellerRequirements_displaysAskingPrice()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRole(ClientRole.SELLER)
                .withAskingPrice(new Price("950000")).build();

        assertRequirementsSummary(person, "Asking $950,000");
    }

    @Test
    public void constructor_bothPrices_labelsBudgetAndAskingPrice()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRole(ClientRole.BOTH)
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAskingPrice(new Price("950000"))
                .withAreas(Set.of(new Area("Bishan")))
                .withHousingTypes(Set.of(HousingType.CONDO)).build();

        assertRequirementsSummary(person, "Budget $800,000 to $1,200,000 · Asking $950,000 · Bishan · Condo");
    }

    @Test
    public void constructor_partialRequirements_omitsMissingPartsAndStraySeparators()
            throws ExecutionException, InterruptedException {
        assertRequirementsSummary(new PersonBuilder().withAreas(Set.of(new Area("Bishan"))).build(), "Bishan");
        assertRequirementsSummary(new PersonBuilder().withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO))
                .build(), "Condo, HDB");
        assertRequirementsSummary(new PersonBuilder().withAreas(Set.of(new Area("Tampines")))
                .withHousingTypes(Set.of(HousingType.HDB)).build(), "Tampines · HDB");
        assertRequirementsSummary(new PersonBuilder().withBudget(new Budget(new Price("800000"), new Price("800000")))
                .withHousingTypes(Set.of(HousingType.HDB)).build(), "Budget $800,000 · HDB");
    }

    @Test
    public void constructor_noRequirements_hidesAndUnmanagesSummary()
            throws ExecutionException, InterruptedException {
        assertRequirementsSummary(new PersonBuilder().build(), "");
    }

    @Test
    public void layout_longRequirements_ellipsizesWithoutClippingContent()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withName("Alexandra Elizabeth Montgomery Chua")
                .withEmail("alexandra.elizabeth.montgomery@example.com").withRole(ClientRole.BOTH)
                .withTags("firsttime", "urgent")
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAskingPrice(new Price("1500000"))
                .withAreas(Set.of(new Area("Bishan"), new Area("Tampines"), new Area("Toa Payoh")))
                .withHousingTypes(Set.of(HousingType.HDB, HousingType.CONDO)).build();
        runOnFxThread(() -> {
            Region root = new PersonCard(person, 1).getRoot();
            createStyledScene(root, 318, 90);
            layoutContentSizedCard(root, 318);

            Label summary = (Label) root.lookup("#requirementsSummary");
            // Font-dependent card height is checked against the real list viewport in MainWindowTest.
            assertTrue(summary.getWidth() < summary.prefWidth(-1));
            assertEquals(OverrunStyle.ELLIPSIS, summary.getTextOverrun());
            assertFalse(summary.isWrapText());
            assertEquals(4, ((VBox) root.lookup("#cardContent")).getChildren().stream()
                    .filter(javafx.scene.Node::isManaged).count());
            assertCardContentFits(root);
            Bounds cardBounds = root.localToScene(root.getLayoutBounds());
            for (String id : List.of("identityRow", "contactRow", "tags", "requirementsSummary")) {
                Region row = (Region) root.lookup("#" + id);
                Bounds bounds = row.localToScene(row.getLayoutBounds());
                assertTrue(bounds.getMinY() >= cardBounds.getMinY());
                assertTrue(bounds.getMaxY() <= cardBounds.getMaxY());
                assertTrue(bounds.getMinX() >= cardBounds.getMinX());
                assertTrue(bounds.getMaxX() <= cardBounds.getMaxX());
            }
        });
    }

    @Test
    public void layout_windowFontChanges_inheritsFontAndFitsContent()
            throws ExecutionException, InterruptedException {
        Person person = new PersonBuilder().withRole(ClientRole.BOTH).withTags("urgent")
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAskingPrice(new Price("950000")).build();
        runOnFxThread(() -> {
            Region card = new PersonCard(person, 1).getRoot();
            StackPane window = new StackPane(card);
            createStyledScene(window, 318, 120);
            window.applyCss();
            layoutContentSizedCard(card, 318);
            List<Label> labels = card.lookupAll(".label").stream().map(node -> (Label) node).toList();
            String nativeFamily = Font.font("System", 13).getFamily();
            assertTrue(labels.stream().allMatch(label -> label.getFont().getFamily().equals(nativeFamily)),
                    "Card text must use the native window font rather than a pinned typeface");

            // Font metrics may change; every row must continue to contain its actual text.
            for (String family : List.of("Monospaced", "Serif")) {
                window.setStyle("-fx-font-family: '" + family + "';");
                window.applyCss();
                layoutContentSizedCard(card, 318);
                String expectedFamily = Font.font(family, 13).getFamily();
                assertTrue(labels.stream().allMatch(label -> label.getFont().getFamily().equals(expectedFamily)),
                        "Card text must inherit " + family);
                assertCardContentFits(card);
            }
        });
    }

    @Test
    public void layout_largerFonts_growsWithTextWithoutClippingRows()
            throws ExecutionException, InterruptedException {
        Person client = new PersonBuilder().withRole(ClientRole.BOTH).withTags("urgent").build();
        Person clientWithRequirements = new PersonBuilder(client)
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAskingPrice(new Price("950000")).build();
        runOnFxThread(() -> {
            for (Person person : List.of(client, clientWithRequirements)) {
                Region root = new PersonCard(person, 1).getRoot();
                createStyledScene(root, 700, 300);
                layoutContentSizedCard(root, 700);
                double originalHeight = root.getHeight();

                root.lookupAll(".label").forEach(label -> label.setStyle("-fx-font-size: 28px;"));
                layoutContentSizedCard(root, 700);

                assertTrue(root.getHeight() > originalHeight, "Larger text must increase the card's height");
                assertCardContentFits(root);

                root.lookupAll(".label").forEach(label -> label.setStyle(""));
                layoutContentSizedCard(root, 700);
                assertEquals(originalHeight, root.getHeight(), 0.01);
            }
        });
    }

    @Test
    public void layout_availableFontFamilies_fitContentWithoutClipping()
            throws ExecutionException, InterruptedException {
        Person taggedClient = new PersonBuilder().withRole(ClientRole.BOTH).withTags("urgent").build();
        Person untaggedClient = new PersonBuilder(taggedClient).withTags().build();
        Person clientWithRequirements = new PersonBuilder(taggedClient)
                .withBudget(new Budget(new Price("800000"), new Price("1200000")))
                .withAskingPrice(new Price("950000")).build();
        runOnFxThread(() -> {
            List<String> families = List.of("System", "Serif", "Monospaced", "Segoe UI", "Arial Unicode MS").stream()
                    .filter(family -> Font.getFamilies().contains(family)).toList();
            assertFalse(families.isEmpty());
            for (String family : families) {
                assertCardsFitWithFontFamily(taggedClient, untaggedClient, clientWithRequirements, family);
            }
        });
    }

    private static void assertCardsFitWithFontFamily(Person taggedClient, Person untaggedClient,
            Person clientWithRequirements, String family) {
        Region taggedCard = createCardWithFontFamily(taggedClient, family);
        Region untaggedCard = createCardWithFontFamily(untaggedClient, family);
        Region requirementsCard = createCardWithFontFamily(clientWithRequirements, family);

        assertEquals(taggedCard.getHeight(), untaggedCard.getHeight(), 0.01,
                "Removing tags must not change the card height for " + family);
        assertTrue(requirementsCard.getHeight() > taggedCard.getHeight(), "The summary must have its own row");
    }

    private static Region createCardWithFontFamily(Person person, String family) {
        Region card = new PersonCard(person, 1).getRoot();
        createStyledScene(card, 318, 120);
        card.lookupAll(".label").forEach(label -> label.setStyle("-fx-font-family: '" + family + "';"));
        layoutContentSizedCard(card, 318);
        // Replacement fonts may grow the card; default-font compactness is tested separately.
        assertCardContentFits(card);
        return card;
    }

    @Test
    public void getRoleStyleClass_eachRole_returnsColourClass() {
        assertEquals("role-buyer", PersonCard.getRoleStyleClass(ClientRole.BUYER));
        assertEquals("role-seller", PersonCard.getRoleStyleClass(ClientRole.SELLER));
        assertEquals("role-both", PersonCard.getRoleStyleClass(ClientRole.BOTH));
    }

    private static void assertRequirementsSummary(Person person, String expected)
            throws ExecutionException, InterruptedException {
        runOnFxThread(() -> {
            Region root = new PersonCard(person, 1).getRoot();
            createStyledScene(root, 318, 90);
            layoutContentSizedCard(root, 318);
            Label summary = (Label) root.lookup("#requirementsSummary");
            assertEquals(expected, summary.getText());
            assertEquals(!expected.isEmpty(), summary.isVisible());
            assertEquals(!expected.isEmpty(), summary.isManaged());
            assertEquals(expected.isEmpty() ? 3 : 4, ((VBox) root.lookup("#cardContent")).getChildren().stream()
                    .filter(javafx.scene.Node::isManaged).count());
            assertCardContentFits(root);
        });
    }

    private static void layoutContentSizedCard(Region root, double width) {
        // Allow tag visibility and the resulting preferred size to settle after CSS changes.
        for (int i = 0; i < 3; i++) {
            root.applyCss();
            root.resize(width, root.prefHeight(width));
            root.layout();
        }
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

    private static void runOnFxThread(Runnable action) throws ExecutionException, InterruptedException {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get();
    }

    private static void createStyledScene(Region root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
    }
}
