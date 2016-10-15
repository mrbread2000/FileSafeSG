package fssg.filesafesg;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {

    private static boolean init = false;
    private static MessageDigest md;
    private static String encryptionPath = "";

    private static void initialization(){

        if (!init) {
            init = true;

            //encryption path
            encryptionPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            encryptionPath += "/FileSafeSGEncryption/";
            File file = new File(encryptionPath);
            if (!file.exists()) {
                boolean success = file.mkdir();
                if (success) {
                    Log.e("Directory","Directory has been created");
                } else {
                    Log.e("Directory","Directory not created");
                }
            }

            //create message digest
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException err) {
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

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static void scanMedia(String path, Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            File file = new File(path);
            Uri testuri = Uri.fromFile(file);
            //String[] projection = { MediaStore.Images.Media._ID };
            String[] projection = { MediaStore.Files.FileColumns._ID };

            // Match on the file path
            //String selection = MediaStore.Images.Media.DATA + " = ?";
            String selection = MediaStore.Files.FileColumns.DATA + " = ?";
            String[] selectionArgs = new String[] { file.getAbsolutePath() };

            // Query for the ID of the media matching the file path
            //Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri queryUri = MediaStore.Files.getContentUri("external");
            ContentResolver contentResolver = act.getContentResolver();
            Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
            if (c.moveToFirst()) {
                // We found the ID. Deleting the item via the content provider will also remove the file
                //long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                //Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);
                contentResolver.delete(deleteUri, null, null);
            } else {
                // File not found in media store DB
                Log.d("Utility", "File not found.");
            }
            c.close();

        } else {
            Intent scanFileIntent = new Intent(
                    Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            act.sendBroadcast(scanFileIntent);
        }

    }

    public static void popupWindow(Activity activity, String message){

        Button closeBn = (Button) activity.findViewById(R.id.btn_close_popup);
        final PopupWindow popUpWindow = new PopupWindow(activity);

        popUpWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.BOTTOM, 10, 10);

        //popUpWindow.update(50, 50, 320, 90);
        closeBn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                popUpWindow.dismiss();
            }

        });

        TextView txtview = (TextView) activity.findViewById(R.id.txt_view_popup);
        txtview.setText(message);

    }

    public static String getEncryptionDirectory() {

        initialization();

        return encryptionPath;
    }

}
