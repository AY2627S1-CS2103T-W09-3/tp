package seedu.address.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
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
     * Creates a footer showing the save location and the number of displayed clients.
     */
    public StatusBarFooter(Path saveLocation, ObservableList<Person> displayedClients) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        IntegerBinding clientCount = Bindings.size(displayedClients);
        clientCountStatus.textProperty().bind(Bindings.when(clientCount.isEqualTo(1))
                .then("1 client")
                .otherwise(clientCount.asString("%d clients")));
    }

}
