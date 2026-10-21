package seedu.address.model.history;

/**
 * Signals that an undo, redo, or history-transition reversal cannot be performed in the current history state.
 */
public class HistoryOperationException extends Exception {

    /**
     * Creates an exception with the specified detail {@code message}.
     */
    public HistoryOperationException(String message) {
        super(message);
    }
}
