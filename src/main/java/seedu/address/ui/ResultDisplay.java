package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String ERROR_STYLE_CLASS = "error";
    private static final String PENDING_STYLE_CLASS = "pending";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Displays successful feedback and clears previous error and pending styling.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        resultDisplay.getStyleClass().removeAll(ERROR_STYLE_CLASS, PENDING_STYLE_CLASS);
    }

    /**
     * Displays feedback that asks the user to confirm, with styling that shows the confirmation is pending.
     */
    public void setPendingFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser);
        resultDisplay.getStyleClass().add(PENDING_STYLE_CLASS);
    }

    /**
     * Displays failed command feedback with an error prefix and styling.
     */
    public void setErrorFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        setFeedbackToUser("Error: " + feedbackToUser);
        resultDisplay.getStyleClass().add(ERROR_STYLE_CLASS);
    }

}
