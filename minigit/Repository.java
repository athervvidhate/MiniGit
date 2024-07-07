package minigit;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


/** Represents a MiniGit repository.
 *  @author Atherv Vidhate
 */
public class Repository {
    public static final File MINIGIT_PATH = new File(".minigit");
    private StagingArea idx = new StagingArea();
    private File head;
    private File currBranch;
    private String currBranchVal = "temp";


    /** Constructor for Repository class
     *
     *  Sets paths of various files to their respective variables for access.
     */
    public Repository() {
        File cb = Utils.join(MINIGIT_PATH, "branches", "currBranch");
        File h = Utils.join(MINIGIT_PATH, "HEAD");
        File i = Utils.join(MINIGIT_PATH, "index");

        if(cb.canRead()) {
            currBranch = cb;
            currBranchVal = Utils.readContentsAsString(Utils.join(MINIGIT_PATH, "branches", Utils.readContentsAsString(cb)));
        }

        if(h.canRead()) {
            head = h;
        }
        if(i.canRead()) {
            idx = Utils.readObject(i, StagingArea.class);
        }
    }


    /** Initializes a MiniGit Repository.
     *
     * Creates all the directories for a MiniGit repo, as well as making an initial empty commit to begin the version
     * control system.
     */
    public void init() {
        if(MINIGIT_PATH.exists()) {
            System.out.println("A MiniGit version-control system already exists in the current directory.");
            System.exit(0);
        }
        MINIGIT_PATH.mkdir();

        //creating all directories
        File blobs = Utils.join(MINIGIT_PATH, "blobs"); // for blobs, using git's method for naming
        blobs.mkdirs();

        File commits = Utils.join(MINIGIT_PATH, "commits"); // for blobs, using git's method for naming
        commits.mkdirs();


        File branches = Utils.join(MINIGIT_PATH, "branches");
        branches.mkdirs();

        head = Utils.join(MINIGIT_PATH, "HEAD");
        currBranch = Utils.join(MINIGIT_PATH, "branches", "currBranch");

        //sets head to its reference and creates main file in branches/
        try {
            head.createNewFile();
            Utils.writeContents(head, "ref: branches/main");

            currBranch.createNewFile();
            Utils.writeContents(currBranch, Utils.readContentsAsString(head).substring(14));
        } catch (IOException e) {
            System.out.println("error in creating head");
        }

        //creates staging area file
        idx = new StagingArea();
        Utils.join(MINIGIT_PATH, "index");
        Utils.writeObject(Utils.join(MINIGIT_PATH, "index"), idx);

        //creates commit object and updates branches/main to point to the newest commit
        Commit initial = new Commit("initial commit", null, new HashMap<String, String>());
        currBranchVal = Utils.sha1(Utils.serialize(initial));
        Utils.writeContents(Utils.join(MINIGIT_PATH, "branches", Utils.readContentsAsString(currBranch)), currBranchVal);

        //writes the initial commit into commits/
        {
            String comHash = Utils.sha1(Utils.serialize(initial));
            String firstTwo = comHash.substring(0, 2);

            File blobDir = Utils.join(MINIGIT_PATH, "commits");
            blobDir.mkdirs();

            File blobFile = Utils.join(blobDir, comHash);
            try {
                blobFile.createNewFile();
                Utils.writeContents(blobFile, Utils.serialize(initial));
            } catch (IOException e) {
                System.out.println("Error in creating commit blob.");
            }
        }
    }


    /** Adds a file to MiniGit's staging area.
     */
    public void add(String[] args) {
        File given = Utils.join(System.getProperty("user.dir"), args[1]);
        if(given.exists()) { //does the file exist?
            if(idx.containsStagedRemove(args[1])) { //is it the staging area, staged for removal?
                idx.removeFromStageToRemove(args[1]);
                Utils.writeObject(Utils.join(MINIGIT_PATH, "index"), idx);
                return;
            }
            addHelper(given, args[1]);
            Utils.writeObject(Utils.join(MINIGIT_PATH, "index"), idx);
        } else {
            System.out.println("File does not exist.");
            System.exit(1);
        }
    }

    /** Helper method for add(), used to simplify reused code
     */
    public void addHelper(File file, String filename) {
        if(idx.containsStagedAddition(filename)) { // is it in the staging area, staged for addition?
            if(idx.getFileHash(filename).equals(Utils.sha1(Utils.readContents(file)))) { // is it the same version?
                System.exit(2);
            } else { // not in staging area staged for addition
                idx.removeFromStageToAdd(filename);
                idx.stageToAdd(file);
                idx.writeToFile(MINIGIT_PATH, Utils.readContents(file));
            }
        } else {
            if(getLatestCommit().getBlobs().get(filename) != null && getLatestCommit().getBlobs().get(filename).equals(Utils.sha1(Utils.readContents(file)))) { // is it in latest commit & same version?
                return;
            } else if(getLatestCommit().getBlobs().containsKey(filename)){ // is it in latest commit, not same version?
                idx.stageToAdd(file);
                idx.writeToFile(MINIGIT_PATH, Utils.readContents(file)); //this and next case might be the same logic wise, could simplify
            } else { // not in anything, completely brand-new file
                idx.stageToAdd(file);
                idx.writeToFile(MINIGIT_PATH, Utils.readContents(file));
            }
        }
    }

    /** Creates a commit with an inputted message.
     *
     * @param args args[1] is the commit message
     */
    @SuppressWarnings("unchecked")
    public void commit(String[] args) {
        if(args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(4);
        }

        if(idx.getStagedAddition().isEmpty() && idx.getStagedRemove().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(5);
        }

        /*
            gets the most recent commit and the files that it tracked, clones it (because without cloning would be using a reference to the same hashmap in memory),
            then gets all the new untracked files and puts them into currentBlobs to be set into the new commit
        */
        Commit acceptedCommit = getLatestCommit();
        HashMap<String, String> currentBlobs = (HashMap<String, String>) acceptedCommit.getBlobs().clone();
        ArrayList<String> newFiles = new ArrayList<>(idx.getStagedAddition().keySet());
        ArrayList<String> removedFiles = idx.getStagedRemove();
        for (String f: newFiles) {
            currentBlobs.put(f, idx.getFileHash(f));
        }
        for (String f: removedFiles) {
            currentBlobs.remove(f);
        }

        // creates a new commit to be created, creates blobs for the commit, clears staging area and saves it to the file
        Commit com = new Commit(args[1], this.currBranchVal, currentBlobs);
        String comHash = Utils.sha1(Utils.serialize(com));

        File blobDir = Utils.join(MINIGIT_PATH, "commits");
        blobDir.mkdirs();

        File blobFile = Utils.join(blobDir, comHash);
        try {
            blobFile.createNewFile();
            Utils.writeContents(blobFile, Utils.serialize(com));
            currBranchVal = comHash;
            Utils.writeContents(Utils.join(MINIGIT_PATH, "branches", Utils.readContentsAsString(currBranch)), currBranchVal);
            idx.clear();
            Utils.writeObject(Utils.join(MINIGIT_PATH, "index"), idx);
        }
        catch (IOException e) {
            System.out.println("Error in creating commit blob.");
        }
    }

    /** Removes a file the staging area or stages a file to be removed in the next commit.
     *
     * @param args args[1] is the name of the file to be removed
     */
    @SuppressWarnings("unchecked")
    public void rm(String[] args) {
        Commit acceptedCommit = getLatestCommit();
        HashMap<String, String> currentBlobs = (HashMap<String, String>) acceptedCommit.getBlobs().clone();

        if(idx.containsStagedAddition(args[1])) { // checking for file being in staging area
            idx.removeFromStageToAdd(args[1]);

            if (currentBlobs.containsKey(args[1])) { // checks for file being in staging area and tracked in latest commit
                idx.stageToRemove(args[1]);
                Utils.restrictedDelete(args[1]);
            }
        } else {                    // checking for if file not being in the staging area, but tracked in latest commit
            if (currentBlobs.containsKey(args[1])) {
                idx.stageToRemove(args[1]);
                Utils.restrictedDelete(args[1]);
            } else {
                System.out.println("No reason to remove the file.");
                System.exit(6);
            }
        }
        Utils.writeObject(Utils.join(MINIGIT_PATH, "index"), idx);
    }

    /** Gets information about every commit in the current branch
     *
     * Information includes commit file name (hash value), the date it was created, and its message.
     */
    public void log() {
        String latestCommitHash = currBranchVal;
        Commit latest = getLatestCommit();
        logHelper(latest, latestCommitHash);
    }


    /** Helper method for log()
     *
     * This is called recursively all the back to the initial commit.
     */
    public void logHelper(Commit com, String comHash) {
        System.out.println("===");
        System.out.println("commit " + comHash);
        System.out.println("Date: " + com.getTimestamp());
        System.out.println(com.getMessage()+"\n");

        if(com.getParent() == null) {
            return;
        }
        String parentHash = com.getParent();
        logHelper(Utils.readObject(Utils.join(MINIGIT_PATH, "commits", parentHash), Commit.class) , parentHash);
    }

    /** Gets information about every commit in the repository
     *
     * Information includes commit file name (hash value), the date it was created, and its message.
     */
    public void globalLog() {
        File commitsDir = Utils.join(MINIGIT_PATH, "commits");
        ArrayList<String> allCommits = new ArrayList<>(Utils.plainFilenamesIn(commitsDir));
        for(String comHash: allCommits) {
            Commit com = Utils.readObject(Utils.join(commitsDir, comHash), Commit.class);
            System.out.println("===");
            System.out.println("commit " + comHash);
            System.out.println("Date: " + com.getTimestamp());
            System.out.println(com.getMessage()+"\n");
        }
    }

    /** Finds all commits in the repository that have the inputted commit message
     *
     * @param args args[1] is the commit message to find commits with
     */
    public void find(String[] args) {
        File commitsDir = Utils.join(MINIGIT_PATH, "commits");
        ArrayList<String> allCommits = new ArrayList<>(Utils.plainFilenamesIn(commitsDir));
        String output = "";
        for(String comHash: allCommits) {
            Commit com = Utils.readObject(Utils.join(commitsDir, comHash), Commit.class);
            if(com.getMessage().equals(args[1])) {
                output += comHash +"\n";
            }
        }

        if (output.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            System.out.println(output);
        }
    }

    /** Three different cases
     *
     * Case 1: checkout <branch name>:
     * This gets all the files from the head commit of the given branch and copies them into the current working directory.
     *
     * Case 2: checkout -- <filename>
     * This gets the given file from the head commit of the current branch and copies it into the current working directory.
     *
     * Case 3: checkout <commit id> -- <filename>
     * This gets the given file from a given commit and copies it in the current working directory.
     */
    public void checkout(String[] args) {
        if(args.length == 2) { //gets all files from branch, sets current branch
            if(args[1].equals(Utils.readContentsAsString(currBranch))) {
                System.out.println("No need to checkout the current branch.");
                System.exit(7);
            }

            ArrayList<String> allBranches = new ArrayList<String>(Utils.plainFilenamesIn(Utils.join(MINIGIT_PATH, "branches")));
            if(!allBranches.contains(args[1])){
                System.out.println("No such branch exists.");
                System.exit(8);
            }

            String branchHead = Utils.readContentsAsString(Utils.join(MINIGIT_PATH, "branches", args[1]));
            Commit checkout = Utils.readObject(Utils.join(MINIGIT_PATH, "commits", branchHead), Commit.class);

            ArrayList<String> files1 = new ArrayList<String>(Utils.plainFilenamesIn(System.getProperty("user.dir")));
            List<String> ignoreFiles = new ArrayList<String>(Arrays.asList(".DS_Store", "MiniGitTests.java", "proj1.iml", "readme.md"));
            for(String filename: ignoreFiles){ // in the way that it is given to test MiniGit, we call from the MiniGit folder, which contains test files and files for the Git repo
                files1.remove(filename);
            }
            Commit com = getLatestCommit();

            for(String filename: files1) {
                File file = Utils.join(System.getProperty("user.dir"), filename);
                if(!com.getBlobs().containsKey(filename) && checkout.getBlobs().containsValue(Utils.sha1(Utils.readContents(file)))) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(9);
                }
            }

            //checks out all files from given branch's head commit
            HashMap<String, String> blobs = checkout.getBlobs();
            ArrayList<String> files = new ArrayList<>(blobs.keySet());
            for(String f: files1) {
                File checkoutFile = Utils.join(System.getProperty("user.dir"), f);
                if(!checkout.getBlobs().containsKey(f) && com.getBlobs().containsKey(f)) {
                    Utils.restrictedDelete(checkoutFile);
                }
            }

            for(String filename: files) {
                String fileHash = blobs.get(filename);
                byte[] fileContents = Utils.readContents(Utils.join(MINIGIT_PATH, "blobs", fileHash));
                File newVersion = Utils.join(System.getProperty("user.dir"), filename);
                Utils.writeContents(newVersion, fileContents);
            }

            // staging area is cleared if the checked out branch is not the current branch
            if(!Utils.readContentsAsString(currBranch).equals(args[1])) {
                idx.clear();
                Utils.writeObject(Utils.join(MINIGIT_PATH, "index"), idx);
            }

            // sets given branch to current branch
            Utils.writeContents(currBranch, args[1]);
            currBranchVal = Utils.readContentsAsString(Utils.join(MINIGIT_PATH, "branches", args[1]));

        } else if(args.length == 3) { // gets file from latest commit
            String fileHash = getLatestCommit().getBlobs().get(args[2]);
            byte[] fileContents = Utils.readContents(Utils.join(MINIGIT_PATH, "blobs", fileHash));
            File checkoutFile = Utils.join(System.getProperty("user.dir"), args[2]);
            if(checkoutFile.exists()){
                checkoutFile.delete();
            }
            File newVersion = Utils.join(System.getProperty("user.dir"), args[2]);
            Utils.writeContents(newVersion, fileContents);
        } else if (args.length == 4){
            if(!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(10);
            }

            String commitHash ="";
            Commit commit = getLatestCommit(); //temp for the try/catch block

            if (args[1].length() < 40) { // case where given commit id is shortened version of the entire commit id
                ArrayList<String> possCommits = new ArrayList<String>(Utils.plainFilenamesIn(Utils.join(MINIGIT_PATH, "commits")));
                for(String c: possCommits) {
                    if (c.contains(args[1])) {
                        commitHash = c;
                        break;
                    }
                }
            } else {
                commitHash = args[1];
            }

            try {
                commit = Utils.readObject(Utils.join(MINIGIT_PATH, "commits", commitHash), Commit.class);
            } catch (IllegalArgumentException e) {
                System.out.println("No commit with that id exists.");
                System.exit(11);
            }

            if(!commit.getBlobs().containsKey(args[3])) {
                System.out.println("File does not exist in that commit.");
                System.exit(12);
            }

            String fileHash = commit.getBlobs().get(args[3]);
            byte[] fileContents = Utils.readContents(Utils.join(MINIGIT_PATH, "blobs", fileHash));
            File checkoutFile = Utils.join(System.getProperty("user.dir"), args[3]);
            if(checkoutFile.exists()){
                checkoutFile.delete();
            }
            File newVersion = Utils.join(System.getProperty("user.dir"), args[3]);
            Utils.writeContents(newVersion, fileContents);
        }
    }

    /** Creates a new branch in the version control system.
     *
     * @param args args[1] is the name of the branch to be made
     */
    public void branch(String[] args) {
        List<String> currentBranches = Utils.plainFilenamesIn(Utils.join(MINIGIT_PATH, "branches"));

        if(currentBranches.contains(args[1])) {
            System.out.println("A branch with that name already exists.");
            System.exit(13);
        }

        File newBranch = Utils.join(MINIGIT_PATH, "branches", args[1]);
        try {
            newBranch.createNewFile();
            Utils.writeContents(newBranch, currBranchVal);
        } catch (IOException e) {
            System.out.println("Error in creating new branch.");
        }
    }

    /** Removes a branch from the version control system, but does not remove the version of the files tracked in
     *  the commits of the branch.
     *
     * @param args args[1] is the name of the branch to be removed
     */
    public void rmBranch(String[] args) {
        List<String> branches = Utils.plainFilenamesIn(Utils.join(MINIGIT_PATH, "branches"));
        if(!branches.contains(args[1])) {
            System.out.println("A branch with that name does not exist.");
            System.exit(14);
        } else if(args[1].equals(Utils.readContentsAsString(currBranch))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(15);
        } else {
            Utils.join(MINIGIT_PATH, "branches", args[1]).delete();
        }
    }

    /** Gets all files from a given commit and copies them into the current working directory, then changes the head
     * commit of the branch to be the given commit.
     *
     * @param args args[1] is the name of the file to be removed
     */
    public void reset(String[] args) {
        Commit checkout = new Commit(null, null, null);
        try {
            checkout = Utils.readObject(Utils.join(MINIGIT_PATH, "commits", args[1]), Commit.class);
        } catch (IllegalArgumentException e) {
            System.out.println("No commit with that id exists.");
            System.exit(16);
        }

        ArrayList<String> files = new ArrayList<String>(Utils.plainFilenamesIn(System.getProperty("user.dir")));
        List<String> ignoreFiles = new ArrayList<String>(Arrays.asList(".DS_Store", "MiniGitTests.java", "proj1.iml", "readme.md"));
        for(String filename: ignoreFiles){ // in the way that it is given to test MiniGit, we call from the MiniGit folder, which contains test files and files for the Git repo
            files.remove(filename);
        }

        Commit com = getLatestCommit();

        for(String filename: files) {
            if(!com.getBlobs().containsKey(filename) && checkout.getBlobs().containsKey(filename)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(17);
            }
        }

        //checks out all files from given branch's head commit
        HashMap<String, String> blobs = checkout.getBlobs();
        ArrayList<String> files1 = new ArrayList<>(blobs.keySet());
        for(String file: files) {
            File checkoutFile = Utils.join(System.getProperty("user.dir"), file);
            if(!checkout.getBlobs().containsKey(file) && com.getBlobs().containsKey(file)) {
                Utils.restrictedDelete(checkoutFile);
            }
        }

        for(String filename: files1) {
            String fileHash = blobs.get(filename);
            byte[] fileContents = Utils.readContents(Utils.join(MINIGIT_PATH, "blobs", fileHash));
            File newVersion = Utils.join(System.getProperty("user.dir"), filename);
            Utils.writeContents(newVersion, fileContents);
        }

        Utils.writeContents(Utils.join(MINIGIT_PATH, "branches", Utils.readContentsAsString(Utils.join(MINIGIT_PATH, "branches", "currBranch"))), args[1]);
        currBranchVal = Utils.readContentsAsString(Utils.join(MINIGIT_PATH, "branches", Utils.readContentsAsString(currBranch)));

        idx.clear();
        Utils.writeObject(Utils.join(MINIGIT_PATH, "index"), idx);
    }

    /** Gets the name of all the branches that exist, displays the files currently staged for addition and removal,
     * and modifications for files that aren't in the staging area and completely untracked files.
     */
    public void status() {
        List<String> branches = Utils.plainFilenamesIn(Utils.join(MINIGIT_PATH, "branches"));
        List<String> allFiles = new ArrayList<>(Utils.plainFilenamesIn(System.getProperty("user.dir")));

        List<String> ignoreFiles = new ArrayList<String>(Arrays.asList(".DS_Store", "MiniGitTests.java", "proj1.iml", "readme.md"));
        for(String filename: ignoreFiles){ // in the way that it is given to test MiniGit, we call from the MiniGit folder, which contains test files and files for the Git repo
            allFiles.remove(filename);
        }

        System.out.println("=== Branches ===");
        for(String b: branches) {
            if(b.equals(Utils.readContentsAsString(head).substring(14))){
                System.out.print("*");
            }
            if(b.equals("currBranch")) {
                continue;
            }
            System.out.println(b);
        }

        System.out.println("\n=== Staged Files ===");
        List<String> sortedList = new ArrayList<>(idx.getStagedAddition().keySet());
        Collections.sort(sortedList);
        for(String file: sortedList) {
            System.out.println(file);
        }

        System.out.println("\n=== Removed Files ===");
        for(String file: idx.getStagedRemove()) {
            System.out.println(file);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for(String filename: allFiles) {
            File file = Utils.join(System.getProperty("user.dir"), filename);
            try {
                Utils.readContents(file);
            } catch (IllegalArgumentException e) {
                System.out.println(filename + " (modified)");
                break;
            }
            if(idx.getStagedAddition().containsKey(filename) && !(idx.getStagedAddition().get(filename).equals(Utils.sha1(Utils.readContents(file))))) { // if older version of file in staging area
                System.out.println(filename + " (modified)");
            } else if(getLatestCommit().getBlobs().containsKey(filename) && !getLatestCommit().getBlobs().get(filename).equals(Utils.sha1(Utils.readContents(file))) && !idx.getStagedAddition().containsKey(filename)) { //if older version of file in latest commit
                System.out.println(filename + " (modified)");
            } else if(!idx.containsStagedRemove(filename) && getLatestCommit().getBlobs().containsKey(filename) && !getLatestCommit().getBlobs().get(filename).equals(Utils.sha1(Utils.readContents(file)))) {
                System.out.println(filename + " (modified)");
            }
        }

        for(String filename: idx.getStagedRemove()) {
            if(!allFiles.contains(filename) && !idx.getStagedRemove().contains(filename)) {
                System.out.println(filename + " (deleted)");
            }
        }


        System.out.println("\n=== Untracked Files ===");
        for(String file: allFiles) {
            if(!getLatestCommit().getBlobs().containsKey(file) && !idx.getStagedAddition().containsKey(file) && !idx.getStagedRemove().contains(file)) {
                System.out.println(file);
            }
        }

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
        System.exit(18);
    }

    /** Gets the latest commit that the HEAD file points to
     *
     * @return Returns the most recent Commit
     */
    public Commit getLatestCommit() {
        return Utils.readObject(Utils.join(MINIGIT_PATH, "commits", currBranchVal), Commit.class);
    }
}
