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

    //adds to hashtable
    public void stageToAdd(File x) {
        toAdd.put(x.getName(), Utils.sha1(Utils.readContents(x)));
    }

    //removes a single element from the toAdd hashtable
    public boolean removeFromStageToAdd(String key) {
        return toAdd.remove(key) != null;
    }

    //removes a single element from the toRemove ArrayList
    public boolean removeFromStageToRemove(String key) {
        return toRemove.remove(key);
    }

    public void stageToRemove(String filename) {
        toRemove.add(filename);
    }

    //checks if a filename exists in the toAdd hashtable
    public boolean containsStagedAddition(String val) {
        return toAdd.containsKey(val);
    }

    //checks if a filename exists in the toRemove ArrayList
    public boolean containsStagedRemove(String val) {
        return toRemove.contains(val);
    }

    //gets the SHA-1 Hash of a file given a filename
    public String getFileHash(String filename) {
        return toAdd.get(filename);
    }

    //clears both the addition and remove staging areas
    public void clear() {
        toAdd = new HashMap<String, String>();
        toRemove = new ArrayList<String>();
    }

    //returns the entire addition staging area as a hashtable
    public HashMap<String, String> getStagedAddition() {
//        HashMap<String, String> out = toAdd;
//        toAdd = new HashMap<String, String>();
        return toAdd;
    }

    //returns the entire remove staging area as an arraylist
    public ArrayList<String> getStagedRemove() {
//        ArrayList<String> out = toRemove;
//        toRemove = new ArrayList<String>();
        return toRemove;
    }

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
