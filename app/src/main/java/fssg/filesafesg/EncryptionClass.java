package fssg.filesafesg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by MrBread2000 on 30/09/16.
 */
public class EncryptionClass extends Activity {

    private int count = 0;
    private ArrayList<String> displayName;
    private ArrayList<Boolean> thumbnailsselection;
    private ArrayList<String> arrPath;
    private ImageAdapter imageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_class);
        // BaseColumns.DATA
        arrPath = new ArrayList<>();
        thumbnailsselection = new ArrayList<>();
        displayName = new ArrayList<>();

        String path = Utility.getEncryptionDirectory();
        File f = new File(path);
        File[] files = f.listFiles();

        ListView docList = (ListView) findViewById(R.id.listView);
        imageAdapter = new ImageAdapter();

        for (File inFile : files) {
            if (inFile.isDirectory()) {
                imageAdapter.addItem(inFile.getAbsolutePath());
            }
        }

        docList.setAdapter(imageAdapter);


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
                scanMedia(path);
                if (imageAdapter != null)
                    imageAdapter.remove(i);
                i--;
            }
        }

    }



    public void decrypt(View view) {
        if (thumbnailsselection == null)
            return;
        for (int i = 0; i < thumbnailsselection.size(); i++) {
            boolean selected = thumbnailsselection.get(i);
            if (selected) {
                String path = arrPath.get(i);
                File filein = new File(path);
                if (filein != null && filein.exists()){
                    String decryptionPathDir = getFileFolderDirectory(path);
                    File fileout = new File(decryptionPathDir, "Y" + filein.getName());
                    Log.d("Decrypte", fileout.getAbsolutePath());
                    try {
                        CryptoUtility.decrypt("password", "salt", filein, fileout);
                        //Utility.popupWindow(this, "Encryption Successful!");
                    } catch (Exception e){
                        System.out.println("Error encrypting file:\n" + e);
                    }
                }
                scanMedia(path);
            }
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
        return targetDirectory;
    }

    private void scanMedia(String path) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<String> filePaths = new ArrayList<String>();

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

        public void addItem(String path){
            filePaths.add(path);
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
            /*
            holder.textView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    int id = v.getId();
                    decryptFile(parent.getContext(), new File(arrPath.get(id)));
                }
            });
            */
            holder.textView.setText(displayName.get(position));

            holder.checkbox.setChecked(thumbnailsselection.get(position));
            holder.id = position;
            return convertView;
        }
    }

    class ViewHolder {
        TextView textView;
        CheckBox checkbox;
        int id;
    }

}
