package fssg.filesafesg;

import java.util.ArrayList;

/**
 * Created by MrBread2000 on 16/11/16.
 */
public class SharedPreference {

    public static ArrayList<Integer> pendingDeletionIntArray = new ArrayList<Integer>();
    public static int successfulFileCount = 0;
    public static int expectedFileCount = 0;
    public static String targetCacheToClearDir = "";

}
