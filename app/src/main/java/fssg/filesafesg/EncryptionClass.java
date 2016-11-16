package fssg.filesafesg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import javax.crypto.Cipher;

/**
 * Created by MrBread2000 on 30/09/16.
 */
public class EncryptionClass extends Activity {

    private int count = 0;
    private ArrayList<String> displayName;
    private ArrayList<Boolean> thumbnailsselection;
    private ArrayList<EncFile> arrEncFiles;
    private EncryptionAdapter encryptionAdapter;

    private ArrayList<Integer> pendingDeletionArr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_class);

        arrEncFiles = new ArrayList<EncFile>();
        thumbnailsselection = new ArrayList<>();
        displayName = new ArrayList<>();

        //Get files in designated encryption directory
        String path = Utility.getEncryptionDirectory();
        File f = new File(path);
        File[] files = f.listFiles();

        //populate files
        for (File inFile : files) {
            if (inFile.exists()) {
                EncFile ef = new EncFile(inFile.getName(), inFile.getAbsolutePath(), inFile.length(), false);
                //ef.debug();
                arrEncFiles.add(ef);
            }
        }

        //create adapter
        encryptionAdapter = new EncryptionAdapter(this, arrEncFiles);

        //Attach adapter to ListView
        ListView encryptedList = (ListView) findViewById(R.id.listView);
        encryptedList.setAdapter(encryptionAdapter);

//------------------------------//
        Button btn_share = (Button) findViewById(R.id.shareit);
        btn_share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shareIt();
            }
        });
    }
    private void shareIt(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        if (arrEncFiles == null)
            return;
        for (int i = 0; i < arrEncFiles.size(); i++) {
            EncFile ef = arrEncFiles.get(i);
            if (ef.ticked) {
                File file = new File(ef.path);
                if (file != null && file.exists())


                //email-sharing
                sharingIntent.setType("*/*");
                sharingIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {""});
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "sending encrypted file ");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        }





    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        //clear cache
        Utility.trimCache(this);

        //back to main page
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

    @Override
    public void onStart(){
        super.onStart();
        //findViewById(R.id.encLoadingBar).setVisibility(View.GONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 77) {
            if(resultCode == CryptoUtility.CRYPTO_FAILED){
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                        "Decryption failed. Password input possibly wrong.",
                        Snackbar.LENGTH_SHORT);
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            } else if (resultCode == RESULT_OK){
                if (pendingDeletionArr != null) {
                    while (pendingDeletionArr.size() > 0) {
                        encryptionAdapter.remove(pendingDeletionArr.remove(pendingDeletionArr.size() - 1));
                    }
                }
            }
        }
    }

    public void delete(View view) {

        if (arrEncFiles == null)
            return;
        for (int i = 0; i < arrEncFiles.size(); i++) {
            EncFile ef = arrEncFiles.get(i);
            if (ef.ticked) {
                File file = new File(ef.path);
                if (file != null && file.exists())
                    file.delete();
                MediaScanner.deleteMedia(ef.path, this);
                encryptionAdapter.remove(i);
                i--;
            }
        }

    }



    public void decrypt(View view) {
        if (arrEncFiles == null)
            return;

        //parse through files
        ArrayList<String> innames = new ArrayList<String>();
        ArrayList<String> targetPathDirs = new ArrayList<String>();
        ArrayList<String> outnames = new ArrayList<String>();
        pendingDeletionArr = new ArrayList<Integer>();
        for (int i = 0; i < arrEncFiles.size(); i++) {
            EncFile ef = arrEncFiles.get(i);
            if (ef.ticked) {
                File filein = new File(ef.path);
                if (filein != null && filein.exists()){

                    innames.add(ef.path);
                    targetPathDirs.add(getFileFolderDirectory(ef.path));
                    outnames.add(filein.getName().replace(".fsg",""));

                    //Add value to be deleted later after encryption is success
                    pendingDeletionArr.add(i);
                }
            }
        }



        //Do encryptions
        if (innames.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), CryptoUtility.class);
            intent.putExtra(CryptoUtility.CIPHER_MODE, Cipher.DECRYPT_MODE);
            intent.putExtra(CryptoUtility.DELETE_AFTER_CIPHER, true);
            intent.putExtra(CryptoUtility.READ_AFTER_CIPHER, false);
            intent.putExtra(CryptoUtility.IN_NAMES, innames);
            intent.putExtra(CryptoUtility.TARGET_DIR_PATHS, targetPathDirs);
            intent.putExtra(CryptoUtility.OUT_NAMES, outnames);
            //startActivity(intent);
            startActivityForResult(intent, 77);
        }
    }


    public void decrypt(EncFile ef) {
        if (ef == null) {
            Log.d("Error", "Null value passed");
            return;
        }

        //parse through files
        ArrayList<String> innames = new ArrayList<String>();
        ArrayList<String> targetPathDirs = new ArrayList<String>();
        ArrayList<String> outnames = new ArrayList<String>();
        pendingDeletionArr = new ArrayList<Integer>(); //clear pending deletion

        File filein = new File(ef.path);
        if (filein != null && filein.exists()) {
            innames.add(ef.path);
            targetPathDirs.add(getApplicationContext().getExternalCacheDir().getAbsolutePath());
            outnames.add(filein.getName().replace(".fsg", ""));
        }

        //Do encryptions
        if (innames.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), CryptoUtility.class);
            intent.putExtra(CryptoUtility.CIPHER_MODE, Cipher.DECRYPT_MODE);
            intent.putExtra(CryptoUtility.DELETE_AFTER_CIPHER, false);
            intent.putExtra(CryptoUtility.READ_AFTER_CIPHER, true);
            intent.putExtra(CryptoUtility.IN_NAMES, innames);
            intent.putExtra(CryptoUtility.TARGET_DIR_PATHS, targetPathDirs);
            intent.putExtra(CryptoUtility.OUT_NAMES, outnames);
            //startActivity(intent);
            startActivityForResult(intent, 77);
        }
    }

    private String getFileFolderDirectory(String path){

        String targetDirectory;

        //Get file's respective folder
        //Image go to image folder
        if (path.contains(".gif")
                || path.contains(".jpg")
                || path.contains(".jpeg")
                || path.contains(".png")
                ) {
            targetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        //video go to video folder
        } else if (path.contains(".3gp")
                || path.contains(".mpg")
                || path.contains(".mpeg")
                || path.contains(".mpe")
                || path.contains(".mp4")
                || path.contains(".avi")
                || path.contains(".flv")) {
            targetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
        //everythig else go to document folder
        } else {
            targetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        }

        //create folder if it doesn't exist
        File folder = new File(targetDirectory);
        if (!folder.exists()) {
            boolean success = folder.mkdir();
            if (success) {
                Log.d("Encrypte", "Folder created at " + targetDirectory);
            } else {
                Log.d("Encrypte", "Failed to create folder at " + targetDirectory);
            }
        }

        return targetDirectory;
    }

    public class EncryptionAdapter extends ArrayAdapter<EncFile> {
        private LayoutInflater mInflater;
        private ArrayList<String> filePaths = new ArrayList<String>();

        private class ViewHolder {
            TextView tvEncName;
            TextView tvEncFileSize;
            CheckBox checkbox;
            Button btnView;
            int id;

        }

        public EncryptionAdapter(Context context, ArrayList<EncFile> arr) {
            super(context, R.layout.encrypted_file_list, arr);
            //mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(EncFile ef){
            arrEncFiles.add(ef);
            notifyDataSetChanged();
        }

        public void remove(int i) {
            arrEncFiles.remove(i);
            notifyDataSetChanged();
        }

        public View getView(final int position, View convertView, final ViewGroup parent) {
            // Get the data item for this position
            //final EncFile ef = getItem(position);
            final EncFile ef = arrEncFiles.get(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.encrypted_file_list, parent, false);
                // Lookup view for data population
                viewHolder.tvEncName = (TextView) convertView.findViewById(R.id.encName);
                viewHolder.tvEncFileSize = (TextView) convertView.findViewById(R.id.encFileSize);
                viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.encCheckBox);
                viewHolder.checkbox.setTag(ef);
                viewHolder.btnView = (Button) convertView.findViewById(R.id.encViewBtn);
                viewHolder.btnView.setTag(ef);

                //checkbox event listener
                viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {

                    //Checkbox
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        EncFile soclef = (EncFile) v.getTag();
                        if (!cb.isChecked()) {
                            cb.setChecked(false);
                            soclef.ticked = false;
                        } else {
                            cb.setChecked(true);
                            soclef.ticked = true;
                        }
                    }
                });

                //View Button listener
                viewHolder.btnView.setOnClickListener(new View.OnClickListener() {

                    //Checkbox
                    public void onClick(View v) {
                        Button cb = (Button) v;
                        EncFile soclef = (EncFile) v.getTag();
                        decrypt(soclef);
                    }
                });

                // Cache the viewHolder object inside the fresh view
                convertView.setTag(viewHolder);
            } else {
                // View is being recycled, retrieve the viewHolder object from tag
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.checkbox.setTag(ef);

            }

            // Populate the data into the template view using the data object
            viewHolder.tvEncName.setText(ef.name);
            //int fs = Ints.checkedCast(ef.fileSize);
            viewHolder.tvEncFileSize.setText("");
            //update checkbox
            viewHolder.checkbox.setChecked(ef.ticked);

            // Return the completed view to render on screen
            return convertView;
        }
    }


    //encrypted files
    class EncFile{
        public String name;
        public String path;
        public long fileSize;
        public boolean ticked;

        public EncFile(String name, String path, long fileSize, boolean ticked){
            this.name = name;
            this.path = path;
            this.fileSize = fileSize;
            this.ticked = ticked;
        }

        public void debug(){
            Log.d("EncFile", "name: " + name + "\npath: " + path + "\nfilesize: " + (int)fileSize + "\nTicked: " + ticked + "\n");
        }
    }


}