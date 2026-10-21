package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        Label emptyListLabel = new Label(Messages.MESSAGE_NO_CLIENTS_FOUND);
        emptyListLabel.getStyleClass().add("label-bright");
        personListView.setPlaceholder(emptyListLabel);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        // A changed list can move another client into the selected index.
        // Commands that retain a selection supply its post-command index separately.
        personList.addListener((ListChangeListener<Person>) change -> clearSelection());
    }

    /**
     * Returns the selected client property, including selections made by clicking a row.
     */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    /**
     * Selects and scrolls to a client in the current list, or clears a stale out-of-range selection.
     */
    public void selectPerson(Index index) {
        int selectedIndex = requireNonNull(index).getZeroBased();
        if (selectedIndex >= personListView.getItems().size()) {
            clearSelection();
            return;
        }
        personListView.getSelectionModel().clearAndSelect(selectedIndex);
        personListView.scrollTo(selectedIndex);
    }

    /**
     * Clears the selected client.
     */
    public void clearSelection() {
        personListView.getSelectionModel().clearSelection();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using
     * a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
