package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_noArguments_returnsUnconfirmedClearCommand() {
        assertParseSuccess(parser, "", new ClearCommand(false));
        assertParseSuccess(parser, "   \t ", new ClearCommand(false));
    }

    @Test
    public void parse_confirm_returnsConfirmedClearCommand() {
        assertParseSuccess(parser, "confirm", new ClearCommand(true));
        assertParseSuccess(parser, " \t confirm  ", new ClearCommand(true));
    }

    @Test
    public void parse_confirmInAnyCase_returnsConfirmedClearCommand() {
        for (String input : List.of("CONFIRM", "Confirm", "cOnFiRm", " \t CONFIRM  ")) {
            assertParseSuccess(parser, input, new ClearCommand(true));
        }
    }

    @Test
    public void parse_unknownArguments_throwsParseException() {
        for (String input : List.of("abc", "3", "confirm now", "confirm confirm", "confirmed", "CONFIRMED", "conf")) {
            assertParseFailure(parser, input, "Invalid command format. Use: clear.");
        }
    }
}
