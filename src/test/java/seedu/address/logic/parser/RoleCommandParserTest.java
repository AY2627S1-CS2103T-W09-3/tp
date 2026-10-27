package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RoleCommand;
import seedu.address.model.person.ClientRole;

public class RoleCommandParserTest {

    private final RoleCommandParser parser = new RoleCommandParser();

    @Test
    public void parse_validArguments_success() {
        assertParseSuccess(parser, " 1 buyer ",
                new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BUYER));
        assertParseSuccess(parser, " 1 SELLER ",
                new RoleCommand(INDEX_FIRST_PERSON, ClientRole.SELLER));
        assertParseSuccess(parser, " 1 Both ",
                new RoleCommand(INDEX_FIRST_PERSON, ClientRole.BOTH));
        assertParseSuccess(parser, " 1 none ",
                new RoleCommand(INDEX_FIRST_PERSON, null));
        assertParseSuccess(parser, " 1 NONE ",
                new RoleCommand(INDEX_FIRST_PERSON, null));
        assertParseSuccess(parser, " 0 none ", new RoleCommand(BigInteger.ZERO, null));
        assertParseSuccess(parser, " 00 buyer ", new RoleCommand(BigInteger.ZERO, ClientRole.BUYER));
        assertParseSuccess(parser, " -1 none ", new RoleCommand(BigInteger.valueOf(-1), null));
        assertParseSuccess(parser, " 2147483648 none ",
                new RoleCommand(new BigInteger("2147483648"), null));
    }

    @Test
    public void parse_invalidFormat_failure() {
        String[] invalidInputs = {
            "", "buyer", "1.5 buyer", "abc buyer", "1 buyer extra"
        };

        for (String input : invalidInputs) {
            assertParseFailure(parser, input, RoleCommand.MESSAGE_INVALID_COMMAND_FORMAT);
        }
    }

    @Test
    public void parse_blankOrInvalidRole_failure() {
        String[] invalidInputs = {"1", "1 agent", "1 buyer-seller"};

        for (String input : invalidInputs) {
            assertParseFailure(parser, input, RoleCommand.MESSAGE_INVALID_ROLE);
        }
    }
}
