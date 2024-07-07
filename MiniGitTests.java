import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MiniGitTests {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    static final Path SRC = Path.of("../test_files");
    static final Path WUG = SRC.resolve("wug.txt");
    static final Path NOTWUG = SRC.resolve("notwug.txt");
    static final Path WUG2 = SRC.resolve("wug2.txt");
    static final Path WUG3 = SRC.resolve("wug3.txt");
    static final String DATE = "Date: \\w\\w\\w \\w\\w\\w \\d+ \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d [-+]\\d\\d\\d\\d";
    static final String COMMIT_HEAD = "commit ([a-f0-9]+)[ \\t]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?" + DATE;
    static final String COMMIT_LOG = "(===[ ]*\\ncommit [a-f0-9]+[ ]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?${DATE}[ ]*\\n(?:.|\\n)*?(?=\\Z|\\n===))"
            .replace("${DATE}", DATE);
    static final String ARBLINE = "[^\\n]*(?=\\n|\\Z)";
    static final String ARBLINES = "(?:(?:.|\\n)*(?:\\n|\\Z)|\\A|\\Z)";

    private static final String COMMAND_BASE = "java miniGit.Main ";
    private static final int DELAY_MS = 150;
    private static final String TESTING_DIR = "testing";

    private static final PrintStream OG_OUT = System.out;
    private static final ByteArrayOutputStream OUT = new ByteArrayOutputStream();

    /**
     * Asserts that the test suite is being run in TESTING_DIR.
     * <p>
     * MiniGit does dangerous file operations, and is affected by the existence
     * of other files. Therefore, we must ensure that we are working in a known
     * directory that (hopefully) has no files.
     */
    public static void verifyWD() {
        Path wd = Path.of(System.getProperty("user.dir"));
        if (!wd.getFileName().endsWith(TESTING_DIR)) {
            fail("This test is not being run in the `testing` directory. " +
                    "Please see the spec for information on how to fix this.");
        }
    }

    @BeforeClass
    public static void setup01_verifyWD() throws FileNotFoundException {
        verifyWD();
    }

    /**
     * Asserts that no class uses nontrivial statics.
     * <p>
     * Using a JUnit tester over a multiple-execution script means that
     * we are running in a single invocation of the JVM, which means that
     * static variables keep their values. Rather than attempting to restore
     * static state (which is nontrivial), we simply ban any static state
     * aside from primitives, Strings (immutable), Paths (immutable),
     * Files (immutable), SimpleDateFormat (not immutable, but can't carry
     * useful info), and a couple utility classes for tests.
     * <p>
     * This test is not a game to be defeated. Even if you manage to smuggle
     * static state, the autograder will test your program by running it
     * over multiple invocations, and your static variables will be reset.
     *
     * @throws IOException
     */
    @BeforeClass
    public static void setup02_noNontrivialStatics() throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        for (String s : System.getProperty("java.class.path")
                .split(System.getProperty("path.separator"))) {
            if (s.endsWith(".jar")) continue;
            Path p = Path.of(s);
            Files.walkFileTree(p, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.toString().endsWith(".idea")) return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!file.toString().toLowerCase().endsWith(".class")) return FileVisitResult.CONTINUE;

                    String qualifiedName = p.relativize(file)
                            .toString()
                            .replace(File.separatorChar, '.');
                    qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 6);
                    try {
                        classes.add(Class.forName(qualifiedName));
                    } catch (ClassNotFoundException ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        List<String> violations = new ArrayList<>();
        List<Class<?>> allowedClasses = List.of(
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                boolean.class,
                char.class,
                String.class,
                Path.class,
                File.class,
                SimpleDateFormat.class,
                // Utils
                FilenameFilter.class,
                // For testing stdout; not actually for use by students.
                ByteArrayOutputStream.class,
                PrintStream.class
        );
        for (Class<?> clazz : classes) {
            List<Field> staticFields = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> Modifier.isStatic(f.getModifiers()))
                    .toList();
            for (Field f : staticFields) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    violations.add("Non-final static field `" + f.getName() + "` found in " + clazz);
                }
                if (!allowedClasses.contains(f.getType())) {
                    violations.add("Static field `" + f.getName() + "` in " + clazz.getCanonicalName() +
                            " is of disallowed type " + f.getType().getSimpleName());
                }
            }
        }

        if (violations.size() > 0) {
            violations.forEach(OG_OUT::println);
            fail("Nontrivial static fields found, see class-level test output for MiniGitTests.\n" +
                    "These indicate that you might be trying to keep global state.");
        }
    }

    @BeforeClass
    public static void setup03_redirectStdout() {
        System.setOut(new PrintStream(OUT));
    }

    public void recursivelyCleanWD() throws IOException {
        // DANGEROUS: We're wiping the directory.
        // Must ensure that we're in the right directory, even though we did in setup01_verifyWD.
        verifyWD();

        // Recursively wipe the directory
        Files.walkFileTree(Path.of(System.getProperty("user.dir")), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                // Don't delete the directory itself, we're about to work in it!
                if (dir.toString().equals(System.getProperty("user.dir"))) {
                    return FileVisitResult.CONTINUE;
                }
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }
        });
    }

    @Before
    public void startWithEmptyWD() throws IOException, InterruptedException {
        recursivelyCleanWD();
        TimeUnit.MILLISECONDS.sleep(DELAY_MS);
    }

    @After
    public void endWithEmptyWD() throws IOException {
        recursivelyCleanWD();
    }

    /**
     * Returns captured output and flush the output stream
     */
    public static String getOutput() {
        String ret = OUT.toString();
        OUT.reset();
        return ret;
    }

    /**
     * Copies a source testing file into the current testing directory.
     *
     * @param src -- Path to source testing file
     * @param dst -- filename to write to; may exist
     */
    public static void writeFile(Path src, String dst) {
        try {
            Files.copy(src, Path.of(dst), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a file from the current testing directory.
     *
     * @param path -- filename to delete; must exist
     */
    public static void deleteFile(String path) {
        try {
            Files.delete(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asserts that a file exists in the current testing directory.
     *
     * @param path
     */
    public static void assertFileExists(String path) {
        if (!Files.exists(Path.of(path))) {
            fail("Expected file " + path + " to exist; does not.");
        }
    }

    /**
     * Asserts that a file does not exist in the current testing directory.
     *
     * @param path
     */
    public static void assertFileDoesNotExist(String path) {
        if (Files.exists(Path.of(path))) {
            fail("Expected file " + path + " to not exist; does.");
        }
    }

    /**
     * Asserts that a file both exists in current testing directory and has
     * identical content to a source testing file.
     *
     * @param src        -- source testing file containin expected content
     * @param pathActual -- filename in current testing directory to check
     */
    public static void assertFileEquals(Path src, String pathActual) {
        assertFileExists(pathActual);
        try {
            String expected = Files.readString(src).replace("\r\n", "\n");
            String actual = Files.readString(Path.of(pathActual)).replace("\r\n", "\n");
            assertEquals("File contents of src file " + src + " and actual file " + pathActual + " are not equal",
                    expected, actual);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copied from Python testing script (`correctProgramOutput`). Intended to adjust for whitespace issues.
     * Removes trailing spaces on lines, and replaces multi-spaces with single spaces.
     *
     * @param s -- string to normalize
     * @return normalized output
     */
    public static String normalizeStdOut(String s) {
        return s.replace("\r\n", "\n")
                .replaceAll("[ \\t]+\n", "\n")
                .replaceAll("(?m)^[ \\t]+", " ");
    }

    /**
     * Asserts that printed content to System.out is correct.
     *
     * @param expected -- expected printed content
     */
    public static void checkOutput(String expected) {
        expected = normalizeStdOut(expected).stripTrailing();
        String actual = normalizeStdOut(getOutput()).stripTrailing();
        assertEquals("ERROR (incorrect output)", expected, actual);
    }

    /**
     * Builds a command-line command from a provided arugments list
     *
     * @param args
     * @return command-line command, i.e. `java MyMain arg1 "arg with space"`
     */
    public static String createCommand(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (arg.contains(" ")) {
                sb.append('"').append(arg).append('"');
            } else {
                sb.append(arg);
            }
        }
        return sb.toString();
    }

    /**
     * Runs the given MiniGit command.
     *
     * @param args
     */
    public static void runMiniGitCommand(String[] args) {
        try {
            OG_OUT.println(COMMAND_BASE + createCommand(args));
            minigit.Main.main(args);
        } catch (SecurityException ignored) {
        } catch (Exception e) {
            // Wrap IOException and other checked for poor implementations;
            // can't directly catch it because it's checked and the compiler complains
            // that it's not thrown
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the given miniGit command and checks the exact output.
     *
     * @param args
     * @param expectedOutput
     */
    public static void miniGitCommand(String[] args, String expectedOutput) {
        runMiniGitCommand(args);
        checkOutput(expectedOutput);
    }

    /**
     * Constructs a regex matcher against the output, for tests to extract groups.
     *
     * @param pattern
     * @return
     */
    public static Matcher checkOutputRegex(String pattern) {
        String actual = getOutput();
        pattern = normalizeStdOut(pattern).stripTrailing();
        String ogP = pattern;
        pattern += "\\Z";
        actual = normalizeStdOut(actual);
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(actual);
        if (!m.matches()) {
            m = p.matcher(actual.stripTrailing());
            if (!m.matches()) {
                // Manually raise a comparison error to get a rich diff for typo catching
                throw new ComparisonFailure("Pattern does not match the output",
                        ogP, actual.stripTrailing());
            }
        }
        return m;
    }

    /**
     * Runs the given miniGit command and checks that the output matches a provided regex
     *
     * @param args
     * @param pattern
     * @return Matcher from the pattern, for group extraction
     */
    public static Matcher miniGitCommandP(String[] args, String pattern) {
        runMiniGitCommand(args);
        return checkOutputRegex(pattern);
    }

    public static void i_prelude1() {
        miniGitCommand(new String[]{"init"}, "");
    }

    public static void i_setup1() {
        i_prelude1();
        writeFile(WUG, "f.txt");
        writeFile(NOTWUG, "g.txt");
        miniGitCommand(new String[]{"add", "g.txt"}, "");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
    }

    public static void i_setup2() {
        i_setup1();
        miniGitCommand(new String[]{"commit", "Two files"}, "");
    }

    public static void i_blankStatus() {
        miniGitCommand(new String[]{"status"}, """
                === Branches ===
                *main

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===
                                
                """);
    }

    public static void i_blankStatus2() {
        miniGitCommand(new String[]{"status"}, """
                === Branches ===
                *main
                other

                === Staged Files ===

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===
                                
                """);
    }

    @Test
    public void test01_init() {
        miniGitCommand(new String[]{"init"}, "");
    }

    @Test
    public void test02_basicCheckout() {
        miniGitCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "added wug"}, "");
        writeFile(NOTWUG, "wug.txt");
        miniGitCommand(new String[]{"checkout", "--", "wug.txt"}, "");
        assertFileEquals(WUG, "wug.txt");
    }

    @Test
    public void test03_basicLog() {
        miniGitCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "added wug"}, "");
        miniGitCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                added wug
                                
                ===
                ${HEADER}
                ${DATE}
                initial commit
                                
                """
                .replace("${HEADER}", "commit [a-f0-9]+")
                .replace("${DATE}", DATE));
    }

    @Test
    public void test04_prevCheckout() {
        miniGitCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
        Matcher logMatch = miniGitCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                version 2 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                version 1 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                initial commit
                                
                """
                .replace("${HEADER}", "commit ([a-f0-9]+)")
                .replace("${DATE}", DATE));
        String uid2 = logMatch.group(1);
        String uid1 = logMatch.group(2);
        miniGitCommand(new String[]{"checkout", uid1, "--", "wug.txt"}, "");
        assertFileEquals(WUG, "wug.txt");
        miniGitCommand(new String[]{"checkout", uid2, "--", "wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
    }

    @Test
    public void test10_initErr() {
        i_prelude1();
        exit.expectSystemExit();
        miniGitCommand(new String[]{"init"}, "A MiniGit version-control system already exists in the current directory.");
    }

    @Test
    public void test11_basicStatus() {
        i_prelude1();
        i_blankStatus();
    }

    @Test
    public void test12_addStatus() {
        i_setup1();
        miniGitCommand(new String[]{"status"}, """
                === Branches ===
                *main

                === Staged Files ===
                f.txt
                g.txt

                === Removed Files ===

                === Modifications Not Staged For Commit ===

                === Untracked Files ===

                """);
    }

    @Test
    public void test13_removeStatus() {
        i_setup2();
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        assertFileDoesNotExist("f.txt");
        miniGitCommand(new String[]{"status"}, """
                === Branches ===
                *main

                === Staged Files ===

                === Removed Files ===
                f.txt

                === Modifications Not Staged For Commit ===

                === Untracked Files ===

                """);
    }

    @Test
    public void test14_addRemoveStatus() {
        i_setup1();
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        miniGitCommandP(new String[]{"status"}, """
                === Branches ===
                \\*main
                                
                === Staged Files ===
                g.txt
                                
                === Removed Files ===
                                
                === Modifications Not Staged For Commit ===
                                
                === Untracked Files ===
                ${ARBLINES}
                                
                """.replace("${ARBLINES}", ARBLINES));
        assertFileEquals(WUG, "f.txt");
    }

    @Test
    public void test15_removeAddStatus() {
        i_setup2();
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        assertFileDoesNotExist("f.txt");
        writeFile(WUG, "f.txt");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        i_blankStatus();
    }

    @Test
    public void test16_emptyCommitErr() {
        i_prelude1();
        exit.expectSystemExit();
        miniGitCommand(new String[]{"commit", "Nothing here"}, "No changes added to the commit.");
    }

    @Test
    public void test17_emptyCommitMessageErr() {
        i_prelude1();
        writeFile(WUG, "f.txt");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"commit", ""}, "Please enter a commit message.");
    }

    @Test
    public void test18_nopAdd() {
        i_setup2();
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        i_blankStatus();
    }

    @Test
    public void test19_addMissingErr() {
        i_prelude1();
        exit.expectSystemExit();
        miniGitCommand(new String[]{"add", "f.txt"}, "File does not exist.");
        i_blankStatus();
    }

    @Test
    public void test20_statusAfterCommit() {
        i_setup2();
        i_blankStatus();
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "Removed f.txt"}, "");
        i_blankStatus();
    }

    @Test
    public void test21_nopRemoveErr() {
        i_prelude1();
        writeFile(WUG, "f.txt");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"rm", "f.txt"}, "No reason to remove the file.");
    }

    @Test
    public void test22_removeDeletedFile() {
        i_setup2();
        deleteFile("f.txt");
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        miniGitCommand(new String[]{"status"}, """
                === Branches ===
                *main
                                
                === Staged Files ===
                                
                === Removed Files ===
                f.txt
                                
                === Modifications Not Staged For Commit ===
                                
                === Untracked Files ===
                                
                """);
    }

    @Test
    public void test23_globalLog() {
        i_setup2();
        String noTzDate = "Date: \\w\\w\\w \\w\\w\\w \\d+ \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d";
        String commitLog = "(===[ ]*\\ncommit [a-f0-9]+[ ]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?${DATE1}) [-+](\\d\\d\\d\\d[ ]*\\n(?:.|\\n)*?(?=\\Z|\\n===))".replace("${DATE1}", noTzDate);
        writeFile(WUG, "h.txt");
        miniGitCommand(new String[]{"add", "h.txt"}, "");
        miniGitCommand(new String[]{"commit", "Add h"}, "");
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                """.replace("${COMMIT_LOG}", commitLog));
        String L1 = m.group(1) + " [-+]" + m.group(2);
        String L2 = m.group(3) + " [-+]" + m.group(4);
        String L3 = m.group(5) + " [-+]" + m.group(6);
        miniGitCommandP(new String[]{"global-log"}, ARBLINES + L1 + ARBLINES);
        miniGitCommandP(new String[]{"global-log"}, ARBLINES + L2 + ARBLINES);
        miniGitCommandP(new String[]{"global-log"}, ARBLINES + L3 + ARBLINES);
    }

    @Test
    public void test24_globalLogPrev() {
        i_setup2();
        String noTzDate = "Date: \\w\\w\\w \\w\\w\\w \\d+ \\d\\d:\\d\\d:\\d\\d \\d\\d\\d\\d";
        String commitLog = "(===[ ]*\\ncommit [a-f0-9]+[ ]*\\n(?:Merge:\\s+[0-9a-f]{7}\\s+[0-9a-f]{7}[ ]*\\n)?${DATE1}) [-+](\\d\\d\\d\\d[ ]*\\n(?:.|\\n)*?(?=\\Z|\\n===))".replace("${DATE1}", noTzDate);
        writeFile(WUG, "h.txt");
        miniGitCommand(new String[]{"add", "h.txt"}, "");
        miniGitCommand(new String[]{"commit", "Add h"}, "");
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                ${COMMIT_LOG}
                """.replace("${COMMIT_LOG}", commitLog));
        String L1 = m.group(1) + " [-+]" + m.group(2);
        m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add h
                                
                ===
                ${COMMIT_HEAD}${ARBLINES}
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD)
                .replace("${ARBLINES}", ARBLINES));
        String id = m.group(2);
        miniGitCommand(new String[]{"reset", id}, "");
        miniGitCommandP(new String[]{"global-log"}, ARBLINES + L1 + "?" + ARBLINES);
    }

    @Test
    public void test25_successfulFind() {
        i_setup2();
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "Remove one file"}, "");
        writeFile(NOTWUG, "f.txt");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "Two files"}, "");
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                Remove one file
                   
                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                initial commit

                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String uid1 = m.group(4);
        String uid2 = m.group(3);
        String uid3 = m.group(2);
        String uid4 = m.group(1);
        miniGitCommandP(
                new String[]{"find", "Two files"},
                "(${UID4}\n${UID2}|${UID2}\n${UID4})"
                        .replace("${UID2}", uid2)
                        .replace("${UID4}", uid4)
        );
        miniGitCommand(new String[]{"find", "initial commit"}, uid1);
        miniGitCommand(new String[]{"find", "Remove one file"}, uid3);
    }

    @Test
    public void test26_successfulFindOrphan() {
        i_setup2();
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "Remove one file"}, "");
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Remove one file
                   
                ===
                ${COMMIT_HEAD}
                Two files

                ===
                ${COMMIT_HEAD}
                initial commit

                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String uid2 = m.group(2);
        String uid3 = m.group(1);
        miniGitCommand(new String[]{"reset", uid2}, "");
        miniGitCommand(new String[]{"find", "Remove one file"}, uid3);
    }

    @Test
    public void test27_unsuccessfulFindErr() {
        i_setup2();
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "Remove one file"}, "");
        miniGitCommand(new String[]{"find", "Add another file"}, "Found no commit with that message.");
    }

    @Test
    public void test28_checkoutDetail() {
        i_prelude1();
        writeFile(WUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
        String header = "commit ([a-f0-9]+)";
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                version 2 of wug.txt

                ===
                ${HEADER}
                ${DATE}
                version 1 of wug.txt

                ===
                ${HEADER}
                ${DATE}
                initial commit

                """.replace("${HEADER}", header).replace("${DATE}", DATE));
        String uid1 = m.group(2);
        miniGitCommand(new String[]{"checkout", uid1, "--", "wug.txt"}, "");
        miniGitCommandP(new String[]{"status"}, """
                === Branches ===
                \\*main
                                
                === Staged Files ===
                                
                === Removed Files ===
                                
                === Modifications Not Staged For Commit ===
                (${ARBLINE}\\n\\r?)?
                === Untracked Files ===
                                
                """.replace("${ARBLINE}", ARBLINE));
    }

    @Test
    public void test29_badCheckoutsErr() {
        i_prelude1();
        writeFile(WUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                version 2 of wug.txt

                ===
                ${COMMIT_HEAD}
                version 1 of wug.txt

                ===
                ${COMMIT_HEAD}
                initial commit

                """.replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String uid2 = m.group(1);
        exit.expectSystemExit();
        miniGitCommand(new String[]{"checkout", uid2, "--", "warg.txt"}, "File does not exist in that commit.");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"checkout", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "--", "wug.txt"},
                "No commit with that id exists.");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"checkout", uid2, "++", "wug.txt"}, "Incorrect operands.");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"checkout", "foobar"}, "No such branch exists.");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"checkout", "main"}, "No need to checkout the current branch.");
    }

    @Test
    public void test30_duplicateBranchErr() {
        i_prelude1();
        miniGitCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG, "f.txt");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "File f.txt"}, "");
        miniGitCommand(new String[]{"checkout", "other"}, "");
        writeFile(NOTWUG, "g.txt");
        miniGitCommand(new String[]{"add", "g.txt"}, "");
        miniGitCommand(new String[]{"commit", "File g.txt"}, "");
        miniGitCommand(new String[]{"checkout", "main"}, "");
        miniGitCommand(new String[]{"rm-branch", "other"}, "");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"checkout", "other"}, "No such branch exists.");
        assertFileDoesNotExist("g.txt");
        assertFileEquals(WUG, "f.txt");
    }

    @Test
    public void test31_duplicateBranchErr() {
        i_prelude1();
        miniGitCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG, "f.txt");
        writeFile(WUG, "g.txt");
        miniGitCommand(new String[]{"add", "g.txt"}, "");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "Main two files"}, "");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"branch", "other"}, "A branch with that name already exists.");
    }

    @Test
    public void test31_rmBranchErr() {
        i_prelude1();
        miniGitCommand(new String[]{"branch", "other"}, "");
        miniGitCommand(new String[]{"checkout", "other"}, "");
        writeFile(WUG, "f.txt");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "File f.txt"}, "");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"rm-branch", "other"}, "Cannot remove the current branch.");
        assertFileExists("f.txt");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"rm-branch", "foo"}, "A branch with that name does not exist.");
    }

    @Test
    public void test32_fileOverwrite() {
        i_prelude1();
        miniGitCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG, "f.txt");
        writeFile(NOTWUG, "g.txt");
        miniGitCommand(new String[]{"add", "g.txt"}, "");
        miniGitCommand(new String[]{"add", "f.txt"}, "");
        miniGitCommand(new String[]{"commit", "Main two files"}, "");
        assertFileExists("f.txt");
        assertFileExists("g.txt");
        miniGitCommand(new String[]{"checkout", "other"}, "");
        writeFile(NOTWUG, "f.txt");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"checkout", "main"}, "There is an untracked file in the way; delete it, or add and commit it first.");
    }

    @Test
    public void test37_reset1() {
        i_setup2();
        miniGitCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        miniGitCommand(new String[]{"add", "h.txt"}, "");
        miniGitCommand(new String[]{"rm", "g.txt"}, "");
        miniGitCommand(new String[]{"commit", "Add h.txt and remove g.txt"}, "");
        miniGitCommand(new String[]{"checkout", "other"}, "");
        miniGitCommand(new String[]{"rm", "f.txt"}, "");
        writeFile(WUG3, "k.txt");
        miniGitCommand(new String[]{"add", "k.txt"}, "");
        miniGitCommand(new String[]{"commit", "Add k.txt and remove f.txt"}, "");
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add k.txt and remove f.txt
                                
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String two = m.group(2);
        miniGitCommand(new String[]{"checkout", "main"}, "");
        m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add h.txt and remove g.txt
                                
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit
                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String main1 = m.group(1);
        writeFile(WUG, "m.txt");
        miniGitCommand(new String[]{"add", "m.txt"}, "");
        miniGitCommand(new String[]{"reset", two}, "");
    }

    @Test
    public void test38_badResetsErr() {
        i_setup2();
        miniGitCommand(new String[]{"branch", "other"}, "");
        writeFile(WUG2, "h.txt");
        miniGitCommand(new String[]{"add", "h.txt"}, "");
        miniGitCommand(new String[]{"rm", "g.txt"}, "");
        miniGitCommand(new String[]{"commit", "Add h.txt and remove g.txt"}, "");
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${COMMIT_HEAD}
                Add h.txt and remove g.txt
                                
                ===
                ${COMMIT_HEAD}
                Two files
                                
                ===
                ${COMMIT_HEAD}
                initial commit

                """
                .replace("${COMMIT_HEAD}", COMMIT_HEAD));
        String main1 = m.group(1);
        miniGitCommand(new String[]{"checkout", "other"}, "");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"reset", "025052f2b193d417df998517a4c539918801b430"}, "No commit with that id exists.");
        writeFile(WUG3, "h.txt");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"reset", main1},
            "There is an untracked file in the way; delete it, or add and commit it first.");
    }

    @Test
    public void test39_shortUid() {
        miniGitCommand(new String[]{"init"}, "");
        writeFile(WUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 1 of wug.txt"}, "");
        writeFile(NOTWUG, "wug.txt");
        miniGitCommand(new String[]{"add", "wug.txt"}, "");
        miniGitCommand(new String[]{"commit", "version 2 of wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
        String header = "commit ([a-f0-9]{8})[a-f0-9]+";
        Matcher m = miniGitCommandP(new String[]{"log"}, """
                ===
                ${HEADER}
                ${DATE}
                version 2 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                version 1 of wug.txt
                                
                ===
                ${HEADER}
                ${DATE}
                initial commit

                """
                .replace("${HEADER}", header)
                .replace("${DATE}", DATE));
        String uid2 = m.group(1);
        String uid1 = m.group(2);
        miniGitCommand(new String[]{"checkout", uid1, "--", "wug.txt"}, "");
        assertFileEquals(WUG, "wug.txt");
        miniGitCommand(new String[]{"checkout", uid2, "--", "wug.txt"}, "");
        assertFileEquals(NOTWUG, "wug.txt");
    }

    @Test
    public void test41_noCommandErr() {
        i_prelude1();
        exit.expectSystemExit();
        miniGitCommand(new String[]{"glorp", "foo"}, "No command with that name exists.");
    }

    @Test
    public void test42_otherErr() {
        exit.expectSystemExit();
        miniGitCommand(new String[]{}, "Please enter a command.");
        exit.expectSystemExit();
        miniGitCommand(new String[]{"status"}, "Not in an initialized MiniGit directory.");
    }

}
