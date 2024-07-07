package minigit;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class StagingArea implements Serializable {
    private HashMap<String, String> toAdd;
    private ArrayList<String> toRemove;

    public StagingArea() {
        toAdd = new HashMap<String, String>();
        toRemove = new ArrayList<String>();
    }

    /** Adds a filename and its hash value to the staging area
     *
     * @param x The File object to be added
     */
    public void stageToAdd(File x) {
        toAdd.put(x.getName(), Utils.sha1(Utils.readContents(x)));
    }

    /** Removes a single element from the toAdd Hashmap
     *
     * @param key The filename to be removed
     * @return whether the file was in the staging area, staged for addition
     */
    //
    public boolean removeFromStageToAdd(String key) {
        return toAdd.remove(key) != null;
    }

    /** Removes a single element from the toRemove ArrayList
     *
     * @param key The filename to be removed
     * @return whether the file was in the staging area, staged for removed
     */
    public boolean removeFromStageToRemove(String key) {
        return toRemove.remove(key);
    }

    /** Stages a file to be removed
     *
     * @param filename The filename to be staged to removed
     */
    public void stageToRemove(String filename) {
        toRemove.add(filename);
    }

    /** Checks if a file exists in the toAdd Hashmap
     *
     * @param val The filename to check for
     * @return whether the file exists in the toAdd hashmap
     */
    public boolean containsStagedAddition(String val) {
        return toAdd.containsKey(val);
    }

    /** Checks if a filename exists in the toRemove ArrayList
     *
     * @param val The filename to check for
     * @return whether the file exists in the toRemove ArrayList
     */
    //
    public boolean containsStagedRemove(String val) {
        return toRemove.contains(val);
    }

    /** Gets the SHA-1 Hash for a given file
     *
     * @param filename The filename to search for
     * @return the SHA-1 hash value
     */
    public String getFileHash(String filename) {
        return toAdd.get(filename);
    }

    /** Clears the staging area
     */
    public void clear() {
        toAdd = new HashMap<String, String>();
        toRemove = new ArrayList<String>();
    }

    /** Returns the entire addition staging area as a hashmap
     *
     * @return The addition staging area
     */
    //
    public HashMap<String, String> getStagedAddition() {
        return toAdd;
    }

    /** Returns the entire removal staging area as a hashmap
     *
     * @return The removal staging area
     */
    //returns the entire remove staging area as an arraylist
    public ArrayList<String> getStagedRemove() {
        return toRemove;
    }

    /** Writes a contents of an object to a file in minigit/blobs/, where the filename is the files SHA-1 Hash value
     *
     * @param startPath the path for the MiniGit repository
     * @param fileContents The contents of the file to write
     */
    //writes the files in the staging area as blobs in objects/
    public void writeToFile(File startPath, byte[] fileContents) {
        Set<String> keys = toAdd.keySet();

        for(String key: keys) {
            File blobDir = Utils.join(startPath, "blobs");
            blobDir.mkdirs();

            File blobFile = Utils.join(blobDir, toAdd.get(key));
        try {
            blobFile.createNewFile();
            Utils.writeContents(blobFile, fileContents);
            }
        catch (IOException e) {
            System.out.println("Error in creating staging area blob.");
            }
        }

    }
}
