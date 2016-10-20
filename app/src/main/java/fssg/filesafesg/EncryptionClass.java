package fssg.filesafesg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import static java.lang.Math.toIntExact;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by MrBread2000 on 30/09/16.
 */
public class EncryptionClass extends Activity {

    private int count = 0;
    private ArrayList<String> displayName;
    private ArrayList<Boolean> thumbnailsselection;
    private ArrayList<EncFile> arrEncFiles;
    private EncryptionAdapter encryptionAdapter;

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
    }

    @Override
    public void onStart(){
        super.onStart();
        findViewById(R.id.encLoadingBar).setVisibility(View.GONE);
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
        //findViewById(R.id.encLoadingBar).setVisibility(View.VISIBLE);
        for (int i = 0; i < arrEncFiles.size(); i++) {
            EncFile ef = arrEncFiles.get(i);
            if (ef.ticked) {
                File filein = new File(ef.path);
                if (filein != null && filein.exists()){
                    String decryptionPathDir = getFileFolderDirectory(ef.path);
                    String outname = filein.getName().replace(".fsg","");
                    File fileout = new File(decryptionPathDir, "Y" + outname);
                    Log.d("Decrypte", fileout.getAbsolutePath());
                    try {
                        CryptoUtility.decrypt("password", "salt", filein, fileout);
                        //Utility.popupWindow(this, "Encryption Successful!");
                    } catch (Exception e){
                        System.out.println("Error encrypting file:\n" + e);
                    }
                    MediaScanner.scanMedia(fileout.getAbsolutePath(), this);
                }
            }
        }
        //findViewById(R.id.encLoadingBar).setVisibility(View.GONE);
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
            Log.d("A",Environment.DIRECTORY_PICTURES);
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
        return targetDirectory;
    }

    public class EncryptionAdapter extends ArrayAdapter<EncFile> {
        private LayoutInflater mInflater;
        private ArrayList<String> filePaths = new ArrayList<String>();

        private class ViewHolder {
            TextView tvEncName;
            TextView tvEncFileSize;
            CheckBox checkbox;
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


                //TEST TEST TEST
                /*
                viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ef.ticked = isChecked;
                        Log.d("Changed", ef.name + ": State now is " + Boolean.toString(ef.ticked));
                    }
                });*/

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
            viewHolder.tvEncFileSize.setText("placeholder");
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