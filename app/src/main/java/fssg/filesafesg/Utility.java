package fssg.filesafesg;


import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {

    private static MessageDigest md;

    public static String hash(String input) {

        if (md == null){
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException err){
                Log.e("FileSafeSG", "No SHA-1 algorithm is found.");
            }
        }

    }

}
