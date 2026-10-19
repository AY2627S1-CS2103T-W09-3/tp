package seedu.address.ui;

import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;

/**
 * Displays one client's profile, or an empty state when no client is selected.
 */
public class DetailPane extends UiPart<Region> {
    private static final String FXML = "DetailPane.fxml";

    @FXML
    private Label emptyState;
    @FXML
    private ScrollPane detailScroll;
    @FXML
    private Label name;
    @FXML
    private Label role;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private VBox budgetRow;
    @FXML
    private Label budget;
    @FXML
    private VBox askingPriceRow;
    @FXML
    private Label askingPrice;
    @FXML
    private VBox preferredAreasRow;
    @FXML
    private Label preferredAreas;
    @FXML
    private VBox housingTypesRow;
    @FXML
    private Label housingTypes;
    @FXML
    private VBox noteRow;
    @FXML
    private Label note;
    @FXML
    private VBox tagsRow;
    @FXML
    private FlowPane tags;

    /**
     * Creates a panel with no selected client.
     */
    public DetailPane() {
        super(FXML);
        setPerson(null);
    }

    /**
     * Replaces the displayed profile. A null person clears the panel.
     */
    public void setPerson(Person person) {
        setShown(emptyState, person == null);
        setShown(detailScroll, person != null);
        detailScroll.setVvalue(0);
        tags.getChildren().clear();
        setShown(tagsRow, false);

        // TODO: Supply the real Person values once the model changes in #87 and #93 are integrated.
        setAdditionalDetails(null, null, null, null, null);

        if (person == null) {
            for (Label label : List.of(name, phone, email, address)) {
                label.setText("");
            }
            setRole(null);
            return;
        }

        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        address.setText(person.getAddress().value);
        setRole(person.getRole());
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    Label tagLabel = new Label(tag.tagName);
                    tagLabel.setWrapText(true);
                    tagLabel.setMinWidth(0);
                    tagLabel.setMinHeight(Region.USE_PREF_SIZE);
                    tagLabel.maxWidthProperty().bind(tags.widthProperty());
                    tags.getChildren().add(tagLabel);
                });
        setShown(tagsRow, !person.getTags().isEmpty());
    }

    /**
     * Renders formatted optional values. This also allows layout previews while the model changes are pending.
     * Null or blank values hide their entire row; note line breaks are preserved.
     */
    void setAdditionalDetails(String budgetText, String askingPriceText, String areasText,
            String housingTypesText, String noteText) {
        setRow(budgetRow, budget, budgetText);
        setRow(askingPriceRow, askingPrice, askingPriceText);
        setRow(preferredAreasRow, preferredAreas, areasText);
        setRow(housingTypesRow, housingTypes, housingTypesText);
        setRow(noteRow, note, noteText);
    }

    private void setRole(ClientRole clientRole) {
        role.getStyleClass().removeAll("role-buyer", "role-seller", "role-both");
        role.setText(clientRole == null ? "" : clientRole.getDisplayValue());
        setShown(role, clientRole != null);
        if (clientRole != null) {
            role.getStyleClass().add(PersonCard.getRoleStyleClass(clientRole));
        }
    }

    private static void setRow(VBox row, Label value, String text) {
        value.setText(text == null ? "" : text);
        setShown(row, text != null && !text.isBlank());
    }

    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }
}
