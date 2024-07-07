package minigit;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable {
    //TODO will probably need to implement the logging functionality here, so we can do log and global log
    private SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
    private String message;
    private Date timestamp;
    private String parent;
    private HashMap<String, String> blobs;

    public Commit(String message, String parent, HashMap<String, String> blobs) {
        //TODO need to also check for removed files
        this.message = message;
        this.parent = parent;
        if (parent == null) {
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
        }
        this.blobs = blobs;

    }


    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return format.format(this.timestamp);
    }

    public String getParent() {
        return this.parent;
    }

}