package seedu.address.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI for the status bar that is displayed at the footer of the application.
 */
public class StatusBarFooter extends UiPart<Region> {

    private static final String FXML = "StatusBarFooter.fxml";

    @FXML
    private Label saveLocationStatus;

    @FXML
    private Label clientCountStatus;

    /**
     * Creates a footer showing the save location, client counts and active filter status.
     */
    public StatusBarFooter(Path saveLocation, ObservableList<Person> displayedClients,
            ObservableList<Person> allClients, ObservableValue<Boolean> isFiltered) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        IntegerBinding displayedCount = Bindings.size(displayedClients);
        IntegerBinding totalCount = Bindings.size(allClients);
        clientCountStatus.textProperty().bind(Bindings.createStringBinding(() -> {
            if (isFiltered.getValue()) {
                return String.format("%d of %d clients (filtered; use list)",
                        displayedCount.get(), totalCount.get());
            }
            return displayedCount.get() == 1 ? "1 client" : displayedCount.get() + " clients";
        }, displayedCount, totalCount, isFiltered));
    }

}
