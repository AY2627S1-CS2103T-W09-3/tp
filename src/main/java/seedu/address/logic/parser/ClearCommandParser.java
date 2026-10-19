package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ClearCommand object
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    /**
     * Parses the given arguments and returns a ClearCommand that is confirmed only if the argument
     * is {@code confirm}.
     *
     * @throws ParseException if the argument is neither empty nor {@code confirm}.
     */
    public ClearCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            return new ClearCommand(false);
        }

        if (trimmedArgs.equals(ClearCommand.CONFIRM_WORD)) {
            return new ClearCommand(true);
        }

        throw new ParseException(ClearCommand.MESSAGE_INVALID_FORMAT);
    }
}
