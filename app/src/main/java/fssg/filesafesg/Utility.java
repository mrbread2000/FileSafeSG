package fssg.filesafesg;


import android.util.Log;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {

    private static MessageDigest md;

    //create message digest
    private static void initialization(){

        if (md == null){
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException err){
                Log.e("FileSafeSG", "No SHA-1 algorithm is found.");
            }
        }


    }

    //Convert hex to string
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //hash using SHA-1
    public static String hash(String sinput) {

        initialization();

        String hashed;
        md.reset();
        md.update(sinput.getBytes(Charset.forName("UTF-8")));
        hashed = bytesToHex(md.digest());

        System.out.println(hashed);

        return hashed;

    }

}
