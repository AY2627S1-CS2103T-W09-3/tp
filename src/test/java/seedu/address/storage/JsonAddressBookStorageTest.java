package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_duplicatePersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("duplicatePersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_legacyFile_preservesPerson() throws Exception {
        ReadOnlyAddressBook legacyAddressBook = readAddressBook("legacyAddressBook.json").get();
        AddressBook expectedAddressBook = new AddressBook();
        expectedAddressBook.addPerson(new PersonBuilder(ALICE).withRole(null)
                .withBudget(null).withAskingPrice(null).withAreas(Set.of()).withHousingTypes(Set.of())
                .build());
        assertEquals(expectedAddressBook, legacyAddressBook);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("migratedAddressBook.json"));
        storage.saveAddressBook(legacyAddressBook);
        assertEquals(expectedAddressBook, storage.readAddressBook().get());
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAddressBook_utf8Bom_loadsClients() throws Exception {
        Path filePath = testFolder.resolve("utf8Bom.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        Files.writeString(filePath, "\uFEFF" + Files.readString(filePath), StandardCharsets.UTF_8);

        assertEquals(original, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAddressBook_utf16LittleEndianBom_loadsClients() throws Exception {
        Path filePath = testFolder.resolve("utf16Bom.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        byte[] jsonBytes = Files.readString(filePath).getBytes(StandardCharsets.UTF_16LE);
        byte[] withBom = new byte[jsonBytes.length + 2];
        withBom[0] = (byte) 0xFF;
        withBom[1] = (byte) 0xFE;
        System.arraycopy(jsonBytes, 0, withBom, 2, jsonBytes.length);
        Files.write(filePath, withBom);

        assertEquals(original, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void saveAddressBook_replacementFails_preservesPreviousFile() throws Exception {
        Path filePath = testFolder.resolve("addressBook.json");
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        new JsonAddressBookStorage(filePath).saveAddressBook(original);
        String previousJson = Files.readString(filePath);

        AddressBook updated = new AddressBook(original);
        updated.addPerson(BOB);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(filePath) {
            @Override
            void replaceDataFile(Path temporaryFile, Path destination) throws IOException {
                throw new IOException("Simulated replacement failure");
            }
        };

        assertThrows(IOException.class, () -> failingStorage.saveAddressBook(updated));
        assertEquals(previousJson, Files.readString(filePath));
        assertEquals(original, new AddressBook(failingStorage.readAddressBook().orElseThrow()));
        try (var files = Files.list(testFolder)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    public void saveAddressBook_replacementAndCleanupFail_preservesOriginalFailure() throws Exception {
        // Error-path combination: replacement and cleanup both fail; the original save failure must remain primary.
        Path filePath = testFolder.resolve("addressBook.json");
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        new JsonAddressBookStorage(filePath).saveAddressBook(original);
        String previousJson = Files.readString(filePath);

        AddressBook updated = new AddressBook(original);
        updated.addPerson(BOB);
        IOException replacementFailure = new IOException("Simulated replacement failure");
        IOException cleanupFailure = new IOException("Simulated cleanup failure");
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(filePath) {
            @Override
            void replaceDataFile(Path temporaryFile, Path destination) throws IOException {
                throw replacementFailure;
            }

            @Override
            void deleteTemporaryFile(Path temporaryFile) throws IOException {
                throw cleanupFailure;
            }
        };

        IOException thrown = assertThrows(
                IOException.class, () -> failingStorage.saveAddressBook(updated));
        assertSame(replacementFailure, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(cleanupFailure, thrown.getSuppressed()[0]);
        assertEquals(previousJson, Files.readString(filePath));
    }

    @Test
    public void saveAddressBook_runtimeReplacementAndCleanupFail_preservesOriginalFailure() throws Exception {
        // A runtime save failure must remain primary even when temporary-file cleanup also fails.
        Path filePath = testFolder.resolve("addressBook.json");
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        new JsonAddressBookStorage(filePath).saveAddressBook(original);
        String previousJson = Files.readString(filePath);

        AddressBook updated = new AddressBook(original);
        updated.addPerson(BOB);
        IllegalStateException replacementFailure = new IllegalStateException("Simulated replacement failure");
        IOException cleanupFailure = new IOException("Simulated cleanup failure");
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(filePath) {
            @Override
            void replaceDataFile(Path temporaryFile, Path destination) {
                throw replacementFailure;
            }

            @Override
            void deleteTemporaryFile(Path temporaryFile) throws IOException {
                throw cleanupFailure;
            }
        };

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class, () -> failingStorage.saveAddressBook(updated));
        assertSame(replacementFailure, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(cleanupFailure, thrown.getSuppressed()[0]);
        assertEquals(previousJson, Files.readString(filePath));
    }

    @Test
    public void saveAddressBook_existingReadOnlyFile_throwsAccessDeniedException() throws Exception {
        Path filePath = testFolder.resolve("readOnlyAddressBook.json");
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        assumeTrue(Files.getFileAttributeView(filePath, PosixFileAttributeView.class) != null);
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(filePath);
        String previousJson = Files.readString(filePath);

        try {
            Files.setPosixFilePermissions(filePath, Set.of(PosixFilePermission.OWNER_READ));
            assumeTrue(!Files.isWritable(filePath));
            assertThrows(AccessDeniedException.class, () -> storage.saveAddressBook(original));
            assertEquals(previousJson, Files.readString(filePath));
        } finally {
            Files.setPosixFilePermissions(filePath, originalPermissions);
        }
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void toPersistedJson_clientsThatRenderTheSame_differWhenStoredValuesDiffer() {
        Person withoutNote = new PersonBuilder().withName("Amy Bee").build();
        Person withNoteText = new PersonBuilder().withName("Amy Bee").withNote("null").build();

        assertNotEquals(JsonAddressBookStorage.toPersistedJson(withoutNote),
                JsonAddressBookStorage.toPersistedJson(withNoteText));
    }

    @Test
    public void toPersistedJson_sameStoredValues_returnsSameText() {
        Person first = new PersonBuilder().withName("Amy Bee").build();
        Person second = new PersonBuilder().withName("Amy Bee").build();

        assertEquals(JsonAddressBookStorage.toPersistedJson(first),
                JsonAddressBookStorage.toPersistedJson(second));
    }
}
