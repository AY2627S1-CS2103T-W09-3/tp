package seedu.address.logic.parser;

import java.util.List;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_BUDGET = new Prefix("b/", true);
    public static final Prefix PREFIX_ASKING_PRICE = new Prefix("s/", true);
    public static final Prefix PREFIX_AREA = new Prefix("l/");
    public static final Prefix PREFIX_HOUSING_TYPE = new Prefix("h/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");
    public static final Prefix PREFIX_ROLE = new Prefix("r/");

    /** Every prefix the product defines, including prefixes that only some commands accept. */
    public static final List<Prefix> ALL_PREFIXES = List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
            PREFIX_ADDRESS, PREFIX_BUDGET, PREFIX_ASKING_PRICE, PREFIX_AREA, PREFIX_HOUSING_TYPE, PREFIX_TAG,
            PREFIX_ROLE);

}
