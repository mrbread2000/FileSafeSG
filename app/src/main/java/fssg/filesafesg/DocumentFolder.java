package fssg.filesafesg;

/**
 * Created by Kevin on 9/27/2016.
 */


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
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

import java.io.File;
import java.util.ArrayList;


public class DocumentFolder extends AppCompatActivity {
    private int count = 0;
    private ArrayList<String> displayName;
    private ArrayList<Boolean> thumbnailsselection;
    private ArrayList<String> arrPath;
    private ImageAdapter imageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        Uri uri = MediaStore.Files.getContentUri("external");
        // BaseColumns.DATA
        String[] projection = {MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.DATA};
        arrPath = new ArrayList<>();
        thumbnailsselection = new ArrayList<>();
        displayName = new ArrayList<>();
        String mime[] = {"doc", "pdf", "txt"};
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

    @Override
    public void onStart(){
        super.onStart();
        findViewById(R.id.docLoadingBar).setVisibility(View.GONE);
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
                Log.d("DocFolder", path);
                if (imageAdapter != null)
                    imageAdapter.remove(i);
                i--;
            }
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


