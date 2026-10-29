package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Email;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Price;
import seedu.address.model.person.PriceRange;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    private static final Pattern PREFIX_TOKEN_PATTERN = Pattern.compile("(?<!\\S)[A-Za-z]{1,3}/");

    /**
     * Every first letter that begins a prefix anywhere in the product, so a prefix belonging to another command,
     * such as {@code r/} in {@code add}, is reported instead of being absorbed into the previous value.
     */
    private static final Set<Character> PREFIX_INITIALS = CliSyntax.ALL_PREFIXES.stream()
            .map(prefix -> prefix.getPrefix().charAt(0))
            .collect(Collectors.toUnmodifiableSet());

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Throws a {@code ParseException} if {@code args} contains a token shaped like a prefix that is not one of
     * {@code knownPrefixes}, so a mistyped prefix is reported instead of being read as part of the previous value.
     *
     * <p>Prefixes are case-sensitive, matching {@link ArgumentTokenizer}: a token counts as known only when it equals
     * a prefix exactly. {@code B/} is therefore reported instead of being accepted as {@code b/} and then ignored by
     * the tokenizer, which would leave the budget text inside the previous value.
     *
     * <p>Only tokens that follow a recognised prefix are reported. A token before the first recognised prefix sits in
     * the preamble, which the command parsers already reject with their own message.
     *
     * <p>An unrecognised token is reported only when its first letter begins a prefix anywhere in the product. That
     * catches a mistyped prefix such as {@code bb/} or {@code pa/}, and also a prefix belonging to another command
     * such as {@code r/} in {@code add}, while leaving ordinary text that contains a slash, such as {@code c/o Mary
     * Tan} or {@code w/ lift} in an address, alone. A word that starts one of the free-text abbreviations, {@code
     * s/o} or {@code b/c}, is left alone in any letter case, because {@link ArgumentTokenizer} also keeps it as text.
     * Within a name, whole relationship markers such as {@code a/l} and {@code A/P} also stay part of the name.
     *
     * @param usage Command usage shown with the error, usually a command's {@code MESSAGE_USAGE}.
     */
    public static void rejectUnknownPrefixes(String args, String usage, Prefix... knownPrefixes)
            throws ParseException {
        requireNonNull(args);
        Set<String> known = new HashSet<>();
        for (Prefix prefix : knownPrefixes) {
            known.add(prefix.getPrefix());
        }
        String currentPrefix = null;
        Matcher matcher = PREFIX_TOKEN_PATTERN.matcher(args);
        while (matcher.find()) {
            if ((CliSyntax.PREFIX_NAME.getPrefix().equals(currentPrefix)
                    && ArgumentTokenizer.isNameRelationshipMarker(args, matcher.start()))
                    || isFreeTextAbbreviation(args, matcher.start(), matcher.end())) {
                continue;
            }
            String token = matcher.group();
            if (known.contains(token)) {
                currentPrefix = token;
            } else if (currentPrefix != null && PREFIX_INITIALS.contains(Character.toLowerCase(token.charAt(0)))) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_PREFIX, token) + "\n" + usage);
            }
        }
    }

    /**
     * Returns true if the token between {@code tokenStart} and {@code tokenEnd} opens a word that is a free-text
     * abbreviation, such as {@code s/o} or {@code b/c}, in any letter case. The check mirrors the one
     * {@link ArgumentTokenizer} applies before it treats a numeric prefix as a new argument.
     */
    private static boolean isFreeTextAbbreviation(String args, int tokenStart, int tokenEnd) {
        int wordEnd = tokenEnd;
        while (wordEnd < args.length() && Character.isLetter(args.charAt(wordEnd))) {
            wordEnd++;
        }
        boolean wordEnds = wordEnd == args.length() || args.charAt(wordEnd) == ' ';
        return wordEnds && ArgumentTokenizer.isFreeTextAbbreviation(args.substring(tokenStart, wordEnd));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed, and each run of spaces inside the name becomes one space.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(Name.collapseSpaces(trimmedName));
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String budget} in the form {@code MIN-MAX} into a {@code Budget}.
     * Leading and trailing whitespaces, including those around the hyphen, will be trimmed.
     *
     * @throws ParseException if either price is invalid or the minimum exceeds the maximum.
     */
    public static Budget parseBudget(String budget) throws ParseException {
        requireNonNull(budget);
        String[] range = budget.trim().split("-", -1);
        if (range.length != 2) {
            throw new ParseException(Price.MESSAGE_CONSTRAINTS);
        }

        Price min = parsePrice(range[0]);
        Price max = parsePrice(range[1]);
        try {
            return new Budget(min, max);
        } catch (IllegalArgumentException e) {
            throw new ParseException(Budget.MESSAGE_CONSTRAINTS, e);
        }
    }

    /**
     * Parses a {@code String priceRange} in the form {@code MIN-MAX}, or a single price, into a {@code PriceRange}.
     * A single price gives a range that holds only that price.
     * Leading and trailing whitespaces, including those around the hyphen, will be trimmed.
     * {@code fieldName} names the field in the error message, such as {@code budget}.
     *
     * @throws ParseException if a range is not two prices joined by a hyphen, a price is invalid or the minimum
     *     exceeds the maximum.
     */
    public static PriceRange parsePriceRange(String priceRange, String fieldName) throws ParseException {
        requireNonNull(priceRange);
        requireNonNull(fieldName);
        String trimmedPriceRange = priceRange.trim();
        if (!trimmedPriceRange.contains("-")) {
            return new PriceRange(parsePrice(trimmedPriceRange));
        }

        String[] range = trimmedPriceRange.split("-", -1);
        if (range.length != 2 || range[0].isBlank() || range[1].isBlank()) {
            throw new ParseException(PriceRange.MESSAGE_INVALID_FORMAT);
        }

        Price min = parsePrice(range[0]);
        Price max = parsePrice(range[1]);
        try {
            return new PriceRange(min, max);
        } catch (IllegalArgumentException e) {
            throw new ParseException(String.format(PriceRange.MESSAGE_REVERSED_RANGE, fieldName), e);
        }
    }

    /**
     * Parses a {@code String price} into a {@code Price}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code price} is invalid.
     */
    public static Price parsePrice(String price) throws ParseException {
        requireNonNull(price);
        String trimmedPrice = price.trim();
        if (!Price.isValidPrice(trimmedPrice)) {
            throw new ParseException(Price.MESSAGE_CONSTRAINTS);
        }
        return new Price(trimmedPrice);
    }

    /**
     * Parses a {@code String area} into an {@code Area}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code area} is not supported.
     */
    public static Area parseArea(String area) throws ParseException {
        requireNonNull(area);
        String trimmedArea = area.trim();
        if (!Area.isValidArea(trimmedArea)) {
            throw new ParseException(Area.MESSAGE_CONSTRAINTS);
        }
        return new Area(trimmedArea);
    }

    /**
     * Parses {@code Collection<String> areas} into a {@code Set<Area>}.
     *
     * @throws ParseException if any area is not supported.
     */
    public static Set<Area> parseAreas(Collection<String> areas) throws ParseException {
        requireNonNull(areas);
        final Set<Area> areaSet = new HashSet<>();
        for (String area : areas) {
            areaSet.add(parseArea(area));
        }
        return areaSet;
    }

    /**
     * Parses a {@code String housingType} into a {@code HousingType}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code housingType} is not supported.
     */
    public static HousingType parseHousingType(String housingType) throws ParseException {
        requireNonNull(housingType);
        String trimmedHousingType = housingType.trim();
        if (!HousingType.isValidHousingType(trimmedHousingType)) {
            throw new ParseException(HousingType.MESSAGE_CONSTRAINTS);
        }
        return HousingType.fromString(trimmedHousingType);
    }

    /**
     * Parses {@code Collection<String> housingTypes} into a {@code Set<HousingType>}.
     *
     * @throws ParseException if any housing type is not supported.
     */
    public static Set<HousingType> parseHousingTypes(Collection<String> housingTypes) throws ParseException {
        requireNonNull(housingTypes);
        final Set<HousingType> housingTypeSet = new HashSet<>();
        for (String housingType : housingTypes) {
            housingTypeSet.add(parseHousingType(housingType));
        }
        return housingTypeSet;
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String note} into a {@code Note}. The escape sequence {@code \n} becomes a line break,
     * while {@code \\} becomes a literal backslash. Other backslashes are preserved.
     * Leading and trailing whitespaces will be trimmed after escape sequences are decoded.
     *
     * @throws ParseException if the decoded {@code note} is invalid.
     */
    public static Note parseNote(String note) throws ParseException {
        requireNonNull(note);
        String decodedNote = decodeNoteEscapes(note).trim();
        if (!Note.isValidNote(decodedNote)) {
            throw new ParseException(Note.MESSAGE_CONSTRAINTS);
        }
        return new Note(decodedNote);
    }

    private static String decodeNoteEscapes(String note) {
        StringBuilder decodedNote = new StringBuilder(note.length());
        for (int i = 0; i < note.length(); i++) {
            char current = note.charAt(i);
            if (current != '\\' || i + 1 == note.length()) {
                decodedNote.append(current);
                continue;
            }

            char next = note.charAt(i + 1);
            if (next == 'n') {
                decodedNote.append('\n');
                i++;
            } else if (next == '\\') {
                decodedNote.append('\\');
                i++;
            } else {
                decodedNote.append(current);
            }
        }
        return decodedNote.toString();
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
