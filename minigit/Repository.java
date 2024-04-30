package minigit;

import java.io.File;


/** Represents a MiniGit repository.
 *  @author TODO
 */
public class Repository {

    public static final File MINIGIT_PATH = new File(".minigit");

    public void init() {
        // TODO
    }

    public void add(String[] args) {
        // TODO
    }

    public void commit(String[] args) {
        // TODO
    }

    public void rm(String[] args) {
        // TODO
    }

    public void log() {
        // TODO
    }

    public void globalLog() {
        // TODO
    }

    public void find(String[] args) {
        // TODO
    }

    public void checkout(String[] args) {
        // TODO
    }

    public void branch(String[] args) {
        // TODO
    }

    public void rmBranch(String[] args) {
        // TODO
    }

    public void reset(String[] args) {
        // TODO
    }

    public void status() {
        // TODO
    }

    public void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            exitWithError("Incorrect operands.");
        }
    }

    public void validateLessThan(String cmd, String[] args, int n) {
        if (args.length > n) {
            exitWithError("Incorrect operands.");
        }
    }

    public void validateInit() {
        if (!MINIGIT_PATH.exists()) {
            exitWithError("Not in an initialized MiniGit directory.");
        }
    }

    public void exitWithError(String message) {
        System.out.println(message);
        System.exit(0);
    }
}
