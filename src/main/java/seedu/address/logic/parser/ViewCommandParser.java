package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ViewCommand}.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Parses the given arguments and returns a {@code ViewCommand}.
     *
     * @throws ParseException If the command format or index is invalid.
     */
    @Override
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        if (!ParserUtil.hasValidIndexSyntax(trimmedArgs)) {
            throw new ParseException(ViewCommand.MESSAGE_INVALID_FORMAT);
        }

        try {
            Index index = ParserUtil.parseIndex(trimmedArgs);
            return new ViewCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, pe);
        }
    }
}
