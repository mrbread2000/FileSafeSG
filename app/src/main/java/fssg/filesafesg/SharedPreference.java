/**
 * Group: SS16/3C
 * Title: Secure File Folder in Android/iOS
 */

package fssg.filesafesg;

import java.util.ArrayList;

public class SharedPreference {

    public static ArrayList<Integer> pendingDeletionIntArray = new ArrayList<Integer>();
    public static int successfulFileCount = 0;
    public static int expectedFileCount = 0;
    public static String targetCacheToClearDir = "";

}
