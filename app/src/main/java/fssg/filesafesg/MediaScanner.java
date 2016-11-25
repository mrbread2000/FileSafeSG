/**
 * Group: SS16/3C
 * Title: Secure File Folder in Android/iOS
 */

package fssg.filesafesg;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class MediaScanner {

    public static void scanMedia(ArrayList<String> paths, Activity act) {
        if ((paths != null) && (!paths.isEmpty())) {
            for (String path : paths) {
                MediaScanner.scanMedia(path, act);
            }
        } else {
            Log.e("MediaScanner", "Nothing to scan");
        }
    }

    public static void scanMedia(String path, Activity act) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        act.sendBroadcast(scanFileIntent);
    }

    public static void deleteMedia(String path, Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File file = new File(path);
            Uri testuri = Uri.fromFile(file);
            String[] projection = {MediaStore.Files.FileColumns._ID};

            // Match on the file path
            String selection = MediaStore.Files.FileColumns.DATA + " = ?";
            String[] selectionArgs = new String[]{file.getAbsolutePath()};

            // Query for the ID of the media matching the file path
            Uri queryUri = MediaStore.Files.getContentUri("external");
            ContentResolver contentResolver = act.getContentResolver();
            Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
            if (c.moveToFirst()) {
                // We found the ID. Deleting the item via the content provider will also remove the file
                long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);
                contentResolver.delete(deleteUri, null, null);
                Log.d("MedisScanner", deleteUri + " has been deleted.");
            } else {
                // File not found in media store DB
                Log.d("MedisScanner", "File not found.");
            }
            c.close();

        } else {
            Intent scanFileIntent = new Intent(
                    Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
            act.sendBroadcast(scanFileIntent);
        }

    }

}
