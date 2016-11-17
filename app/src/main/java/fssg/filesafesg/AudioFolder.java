package fssg.filesafesg;

/**
 * Created by Kevin on 9/27/2016.
 */


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;

import javax.crypto.Cipher;


public class AudioFolder extends AppCompatActivity {
    private int count = 0;
    private ArrayList<String> displayName;
    private ArrayList<Boolean> thumbnailsselection;
    private ArrayList<String> arrPath;
    private ImageAdapter imageAdapter;
    private Toolbar toolbar;

    private ArrayList<Integer> pendingDeletionArr = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        setTitle(R.string.title_activity_audio_folder);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        Uri uri = MediaStore.Files.getContentUri("external");
        // BaseColumns.DATA
        String[] projection = {MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.DATA};
        arrPath = new ArrayList<>();
        thumbnailsselection = new ArrayList<>();
        displayName = new ArrayList<>();
        String mime[] = {"mp3"};
        for (int j = 0; j < mime.length; j++) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mime[j]);
            String[] selectionArgsPdf = new String[]{mimeType};
            Cursor allPdfFiles = getContentResolver().query(uri, projection, selectionMimeType, selectionArgsPdf, null);
            if (allPdfFiles != null && allPdfFiles.moveToFirst()) {
                count += allPdfFiles.getCount();
                int display_name_index = allPdfFiles.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
                int path_index = allPdfFiles.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                do {
                    displayName.add(allPdfFiles.getString(display_name_index));
                    arrPath.add(allPdfFiles.getString(path_index));
                    thumbnailsselection.add(false);
                } while (allPdfFiles.moveToNext());
                allPdfFiles.close();
            }
        }

        ListView docList = (ListView) findViewById(R.id.listView);
        imageAdapter = new ImageAdapter();
        docList.setAdapter(imageAdapter);


    }

    //test code=============================
    private boolean wentToBackground = false;
    @Override
    public void onStop(){
        super.onStop();
        wentToBackground = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (wentToBackground)
            this.finish();
    }

    @Override
    public void onBackPressed(){

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    //======================================


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 77) {
            if(resultCode == CryptoUtility.CRYPTO_FAILED){
                //snack the message
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                        "Encryption failed. There may be an issue with the file you are trying to encrypt.",
                        Snackbar.LENGTH_SHORT);
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            } else if (resultCode == RESULT_OK){
            } else if (resultCode == RESULT_CANCELED) {
                //message interruption
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                        "Encryption has been interrupted.",
                        Snackbar.LENGTH_SHORT);
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();

            }
            //remove from list
            if (SharedPreference.pendingDeletionIntArray != null) {
                while (SharedPreference.pendingDeletionIntArray.size() > 0) {
                    imageAdapter.remove(SharedPreference.pendingDeletionIntArray.remove(SharedPreference.pendingDeletionIntArray.size() - 1));
                }
                SharedPreference.pendingDeletionIntArray.clear();
            }
        }
    }

    public void delete(View view) {

        if (thumbnailsselection == null)
            return;
        for (int i = 0; i < thumbnailsselection.size(); i++) {
            boolean selected = thumbnailsselection.get(i);
            if (selected) {
                String path = arrPath.get(i);
                File file = new File(path);
                if (file != null && file.exists())
                    file.delete();
                MediaScanner.deleteMedia(path, this);
                if (imageAdapter != null)
                    imageAdapter.remove(i);
                i--;
            }
        }

    }

    public void encrypt(View view) {
        if (thumbnailsselection == null)
            return;
        //parse through files
        ArrayList<String> innames = new ArrayList<String>();
        ArrayList<String> targetPathDirs = new ArrayList<String>();
        ArrayList<String> outnames = new ArrayList<String>();
        pendingDeletionArr.clear();
        for (int i = 0; i < thumbnailsselection.size(); i++) {
            boolean selected = thumbnailsselection.get(i);
            if (selected) {
                String path = arrPath.get(i);
                File filein = new File(path);
                if (filein != null && filein.exists()) {

                    innames.add(path);
                    targetPathDirs.add(Utility.getEncryptionDirectory());
                    outnames.add(filein.getName() + ".fsg");

                    pendingDeletionArr.add(i);
                }

            }
        }

        //Do encryptions
        if (innames.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), CryptoUtility.class);
            intent.putExtra(CryptoUtility.CIPHER_MODE, Cipher.ENCRYPT_MODE);
            intent.putExtra(CryptoUtility.DELETE_AFTER_CIPHER, true);
            intent.putExtra(CryptoUtility.IN_NAMES, innames);
            intent.putExtra(CryptoUtility.TARGET_DIR_PATHS, targetPathDirs);
            intent.putExtra(CryptoUtility.OUT_NAMES, outnames);
            intent.putExtra(CryptoUtility.PENDING_DELETION_INT, pendingDeletionArr);
            startActivityForResult(intent,77);
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public void remove(int i) {
            displayName.remove(i);
            thumbnailsselection.remove(i);
            arrPath.remove(i);
            count = count - 1;
            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, final ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.docitem, null);
                holder.textView = (TextView) convertView.findViewById(R.id.documents);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(position);
            holder.textView.setId(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {

                //Checkbox
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    if (thumbnailsselection.get(id)) {
                        cb.setChecked(false);
                        thumbnailsselection.set(id, false);
                    } else {
                        cb.setChecked(true);
                        thumbnailsselection.set(id, true);
                    }
                }
            });
            holder.textView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    int id = v.getId();
                    openFile(parent.getContext(), new File(arrPath.get(id)));
                }
            });
            holder.textView.setText(displayName.get(position));

            holder.checkbox.setChecked(thumbnailsselection.get(position));
            holder.id = position;
            return convertView;
        }

        public void openFile(Context context, File url) {
            File file = url;
            Uri uri = Uri.fromFile(file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            // Check what kind of file user trying to open, by comparing the url with extensions.
            // When the if condition is matched, plugin sets the correct intent (mime) type,
            // so Android knows what application to use to open the file

            if (url.toString().contains(".wav") || url.toString().contains(".mp3")
                    || url.toString().contains(".wma") || url.toString().contains(".aac") || url.toString().contains(".flac")
                    || url.toString().contains(".m4a") || url.toString().contains(".ogg")) {
                intent.setDataAndType(uri, "audio/x-wav");
            } else {
                //Future intent type for any other file types

                //Use this else clause below to manage other unknown extensions
                //Android will show all applications installed on the device(let user choose)
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    class ViewHolder {
        TextView textView;
        CheckBox checkbox;
        int id;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.aud_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.encryptBtn:

                //parse through files
                ArrayList<String> innames = new ArrayList<String>();
                ArrayList<String> targetPathDirs = new ArrayList<String>();
                ArrayList<String> outnames = new ArrayList<String>();
                for (int i = 0; i < thumbnailsselection.size(); i++) {
                    boolean selected = thumbnailsselection.get(i);
                    if (selected) {
                        String path = arrPath.get(i);
                        File filein = new File(path);
                        if (filein != null && filein.exists()) {

                            innames.add(path);
                            targetPathDirs.add(Utility.getEncryptionDirectory());
                            outnames.add(filein.getName() + ".fsg");

                            if (imageAdapter != null)
                                imageAdapter.remove(i);
                            i--;
                        }
                    }
                }

                //Do encryptions
                if (innames.size() > 0) {
                    Intent intent = new Intent(getApplicationContext(), CryptoUtility.class);
                    intent.putExtra(CryptoUtility.CIPHER_MODE, Cipher.ENCRYPT_MODE);
                    intent.putExtra(CryptoUtility.DELETE_AFTER_CIPHER, true);
                    intent.putExtra(CryptoUtility.IN_NAMES, innames);
                    intent.putExtra(CryptoUtility.TARGET_DIR_PATHS, targetPathDirs);
                    intent.putExtra(CryptoUtility.OUT_NAMES, outnames);
                    startActivity(intent);
                }

                return true;

            case R.id.deleteBtn:

                for (int i = 0; i < thumbnailsselection.size(); i++) {
                    boolean selected = thumbnailsselection.get(i);
                    if (selected) {
                        String path = arrPath.get(i);
                        File file = new File(path);
                        if (file != null && file.exists())
                            file.delete();
                        MediaScanner.deleteMedia(path, this);
                        Log.d("AudioFolder", path);
                        if (imageAdapter != null)
                            imageAdapter.remove(i);
                        i--;
                    }
                }
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }
}


