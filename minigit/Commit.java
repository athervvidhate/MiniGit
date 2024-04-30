package minigit;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable {

    private SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
    private String message;
    private Date timestamp;

}