package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import seedu.address.model.person.Area;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private Label name;
    @FXML
    private Label role;
    @FXML
    private Label noteIndicator;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private StackPane tags;
    @FXML
    private Label requirementsSummary;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        setRoleBadge(person.getRole());
        boolean hasNote = person.getNote() != null;
        noteIndicator.setVisible(hasNote);
        noteIndicator.setManaged(hasNote);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        List<String> tagNames = person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(tag -> tag.tagName)
                .toList();
        tags.getChildren().add(new TagRow(tagNames));
        setRequirementsSummary();
    }

    private void setRequirementsSummary() {
        List<String> parts = new ArrayList<>();
        if (person.getBudget() != null) {
            parts.add("Budget " + person.getBudget());
        }
        if (person.getAskingPrice() != null) {
            parts.add("Asking " + person.getAskingPrice().getDisplayValue());
        }
        String areas = String.join(", ", person.getAreas().stream().map(Area::getValue).sorted().toList());
        if (!areas.isEmpty()) {
            parts.add(areas);
        }
        String housingTypes = String.join(", ", person.getHousingTypes().stream()
                .map(HousingType::getDisplayValue).sorted().toList());
        if (!housingTypes.isEmpty()) {
            parts.add(housingTypes);
        }

        requirementsSummary.setText(String.join(" · ", parts));
        boolean hasRequirements = !parts.isEmpty();
        requirementsSummary.setVisible(hasRequirements);
        requirementsSummary.setManaged(hasRequirements);
    }

    private void setRoleBadge(ClientRole clientRole) {
        if (clientRole == null) {
            role.setVisible(false);
            role.setManaged(false);
            return;
        }

        role.setText(clientRole.getDisplayValue());
        role.getStyleClass().add(getRoleStyleClass(clientRole));
    }

    static String getRoleStyleClass(ClientRole clientRole) {
        return switch (clientRole) {
            case BUYER -> "role-buyer";
            case SELLER -> "role-seller";
            case BOTH -> "role-both";
        };
    }

    /**
     * Keeps tags on a single row and exposes any overflow through a count and tooltip.
     */
    private static class TagRow extends HBox {
        private static final double TAG_SPACING = 7;
        private static final double OVERFLOW_TOOLTIP_MAX_WIDTH = 300;

        private final List<String> tagNames;
        private final List<Label> tagLabels;
        private final Label overflow = new Label();
        private final Tooltip overflowTooltip = new Tooltip();
        private final Text overflowMeasure = new Text();

        TagRow(List<String> tagNames) {
            super(TAG_SPACING);
            this.tagNames = tagNames;
            setId("tagRow");
            setMinWidth(0);
            setPrefWidth(0);
            setAlignment(Pos.CENTER_LEFT);
            tagLabels = tagNames.stream().map(name -> {
                Label label = new Label(name);
                label.setMinWidth(Region.USE_PREF_SIZE);
                return label;
            }).toList();
            overflow.setId("tagOverflow");
            overflow.setMinWidth(0);
            overflowTooltip.setWrapText(true);
            overflowTooltip.setMaxWidth(OVERFLOW_TOOLTIP_MAX_WIDTH);
            overflow.setTooltip(overflowTooltip);
            getChildren().addAll(tagLabels);
            getChildren().add(overflow);
        }

        @Override
        protected void layoutChildren() {
            int shown = countTagsThatFit();
            updateVisibleTags(shown);
            updateOverflow(shown);
            super.layoutChildren();
        }

        private int countTagsThatFit() {
            int shown = tagLabels.size();
            double labelWidth = tagLabels.stream().mapToDouble(label -> label.prefWidth(-1)).sum();
            while (shown > 0) {
                double requiredWidth = labelWidth + getSpacing() * (shown - 1);
                if (shown < tagLabels.size()) {
                    requiredWidth += getSpacing() + measureOverflowWidth(tagLabels.size() - shown);
                }
                if (requiredWidth <= getWidth()) {
                    break;
                }
                shown--;
                labelWidth -= tagLabels.get(shown).prefWidth(-1);
            }
            return shown;
        }

        private void updateVisibleTags(int shown) {
            for (int i = 0; i < tagLabels.size(); i++) {
                tagLabels.get(i).setVisible(i < shown);
                tagLabels.get(i).setManaged(i < shown);
            }
        }

        private void updateOverflow(int shown) {
            boolean hasOverflow = shown < tagLabels.size();
            overflow.setVisible(hasOverflow);
            overflow.setManaged(hasOverflow);
            if (hasOverflow) {
                String count = "+" + (tagLabels.size() - shown);
                if (!count.equals(overflow.getText())) {
                    overflow.setText(count);
                }
                String tooltip = "Additional tags: " + String.join(", ", tagNames.subList(shown, tagNames.size()));
                if (!tooltip.equals(overflowTooltip.getText())) {
                    overflowTooltip.setText(tooltip);
                }
            }
        }

        private double measureOverflowWidth(int count) {
            overflowMeasure.setFont(overflow.getFont());
            overflowMeasure.setText("+" + count);
            return Math.ceil(overflowMeasure.getLayoutBounds().getWidth())
                    + overflow.getInsets().getLeft() + overflow.getInsets().getRight()
                    + overflow.getLabelPadding().getLeft() + overflow.getLabelPadding().getRight();
        }
    }
}
