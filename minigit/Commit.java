package minigit;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable {
    private SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
    private String message;
    private Date timestamp;
    private String parent;
    private HashMap<String, String> blobs;

    /** Constructor the Commit class, creates a new commit with given values
     *
     * @param message The commit message
     * @param parent The parent commit
     * @param blobs The files that the commit tracks
     */
    public Commit(String message, String parent, HashMap<String, String> blobs) {
        this.message = message;
        this.parent = parent;
        if (parent == null) {
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
        }
        this.blobs = blobs;

    }

    /** Gets the files that the current commit tracks
     *
     * @return Commit's tracked files
     */
    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    /** Gets the current commit's message
     *
     * @return The commit's message
     */
    public String getMessage() {
        return this.message;
    }

    /** Gets the current commit's timestamp
     *
     * @return The commit's timestamp
     */
    public String getTimestamp() {
        return format.format(this.timestamp);
    }

    /** Gets the current commit's parent commit
     *
     * @return The commit's parent commit
     */
    public String getParent() {
        return this.parent;
    }

}