package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;

import seedu.address.logic.commands.RoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientRole;

/**
 * Parses input arguments and creates a new {@code RoleCommand} object.
 */
public class RoleCommandParser implements Parser<RoleCommand> {

    @Override
    public RoleCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        String[] arguments = trimmedArgs.isEmpty() ? new String[0] : trimmedArgs.split("\\s+");

        if (arguments.length == 1) {
            try {
                ParserUtil.parseIndex(arguments[0]);
            } catch (ParseException pe) {
                throw new ParseException(RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT, pe);
            }
            throw new ParseException(RoleCommand.MESSAGE_INVALID_ROLE);
        }

        if (arguments.length != 2) {
            throw new ParseException(RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }

        if (!arguments[0].matches("-?\\d+")) {
            throw new ParseException(RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }
        BigInteger oneBasedIndex = new BigInteger(arguments[0]);

        if (arguments[1].equalsIgnoreCase("none")) {
            return new RoleCommand(oneBasedIndex, null);
        }

        if (!ClientRole.isValidRole(arguments[1])) {
            throw new ParseException(RoleCommand.MESSAGE_INVALID_ROLE);
        }

        return new RoleCommand(oneBasedIndex, ClientRole.fromString(arguments[1]));
    }
}
