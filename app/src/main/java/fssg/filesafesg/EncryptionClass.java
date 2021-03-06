package fssg.filesafesg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        Uri uri = MediaStore.Files.getContentUri("external");
        // BaseColumns.DATA
        String[] projection = {MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.DATA};
        arrPath = new ArrayList<>();
        thumbnailsselection = new ArrayList<>();
        displayName = new ArrayList<>();
        String mime[] = {"jpg", "pdf", "doc", "docx", "ppt", "txt"};
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
                    String encryptionPathDir = Utility.getEncryptionDirectory();
                    File fileout = new File(encryptionPathDir, "Y" + filein.getName());
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

    private void scanMedia(String path) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
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

            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
                // WAV audio file
                intent.setDataAndType(uri, "application/x-wav");
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
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

}
