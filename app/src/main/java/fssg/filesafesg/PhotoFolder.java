/**
 * Group: SS16/3C
 * Title: Secure File Folder in Android/iOS
 */

package fssg.filesafesg;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import javax.crypto.Cipher;

public class PhotoFolder extends AppCompatActivity {
    private int count;
    private ArrayList<Bitmap> thumbnails;
    private ArrayList<Boolean> thumbnailsselection;
    private ArrayList<String> arrPath;
    private ImageAdapter imageAdapter;
    private Toolbar toolbar;
    private final int THUMBSIZE = 160;
    private ArrayList<Integer> pendingDeletionArr = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        setTitle(R.string.title_activity_photo_folder);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor imagecursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy, null);
        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        this.count = imagecursor.getCount();
        thumbnails = new ArrayList<>();
        arrPath = new ArrayList<>();
        thumbnailsselection = new ArrayList<>();
        for (int i = 0; i < this.count; i++) {
            imagecursor.moveToPosition(i);
            int id = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            //thumbnails.add(MediaStore.Images.Thumbnails.getThumbnail(
            //        getApplicationContext().getContentResolver(), id,
            //        MediaStore.Images.Thumbnails.MICRO_KIND, null));
            arrPath.add(imagecursor.getString(dataColumnIndex));
            thumbnails.add(null);
            thumbnailsselection.add(false);
        }


        GridView imagegrid = (GridView) findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);
        imagecursor.close();

        //hide button lazy way
        Button b = (Button) findViewById(R.id.encryptBtn);
        b.setVisibility(View.GONE);
    }


    //test code=============================
    private boolean wentToBackground = false;

    @Override
    public void onStop() {
        super.onStop();
        wentToBackground = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wentToBackground)
            this.finish();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    //======================================

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 77) {
            if (resultCode == CryptoUtility.CRYPTO_FAILED) {
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                        "Encryption failed. There may be an issue with the file you are trying to encrypt.",
                        Snackbar.LENGTH_SHORT);
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            } else if (resultCode == RESULT_OK) {
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
        } else if (requestCode == DeleteUtility.DELETE_ACTIVITY_RQ_CODE){

            //Delete Handling
            if (resultCode == DeleteUtility.DELETE_YES){
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
            } else if (resultCode == DeleteUtility.DELETE_NO){

            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        findViewById(R.id.photoLoadingBar).setVisibility(View.GONE);
    }

    public void delete(View view) {
        if (thumbnailsselection == null)
            return;
        for (int i = 0; i < thumbnailsselection.size(); i++) {
            boolean selected = thumbnailsselection.get(i);
            if (selected) {
                String path = arrPath.get(i);
                File file = new File(path);
                if (file != null && file.exists()) {
                    file.delete();

                }
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
                    //if (imageAdapter != null)
                    //    imageAdapter.remove(i);
                    //i--;
                }
            }
        }

        if (innames.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), CryptoUtility.class);
            intent.putExtra(CryptoUtility.CIPHER_MODE, Cipher.ENCRYPT_MODE);
            intent.putExtra(CryptoUtility.DELETE_AFTER_CIPHER, true);
            intent.putExtra(CryptoUtility.IN_NAMES, innames);
            intent.putExtra(CryptoUtility.TARGET_DIR_PATHS, targetPathDirs);
            intent.putExtra(CryptoUtility.OUT_NAMES, outnames);
            intent.putExtra(CryptoUtility.PENDING_DELETION_INT, pendingDeletionArr);
            //startActivity(intent);
            startActivityForResult(intent, 77);
        }
    }


    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        int id;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.aud_menu, menu);
        return true;
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
            thumbnails.remove(i);
            thumbnailsselection.remove(i);
            arrPath.remove(i);
            count = count - 1;
            notifyDataSetChanged();

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.galleryitem, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(position);
            holder.imageview.setId(position);
            holder.checkbox.setOnClickListener(new OnClickListener() {

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
            holder.imageview.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    int id = v.getId();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + arrPath.get(id)), "image/*");

                    //check if viewing is supported
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                                "This phone does not support this File type.",
                                Snackbar.LENGTH_SHORT);
                        View view = snack.getView();
                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        snack.show();
                    }
                }
            });
            //holder.imageview.setImageBitmap(thumbnails.get(position));
            if (thumbnails.get(position) == null) {
                //Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(arrPath.get(position)),
                //        THUMBSIZE, THUMBSIZE);
                //thumbnails.set(position, ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(arrPath.get(position)),
                //        THUMBSIZE, THUMBSIZE));
                holder.imageview.setImageBitmap(null);
                holder.imageview.setTag(position);
                new LoadImage(holder.imageview).execute();
            } else {
                holder.imageview.setImageBitmap(thumbnails.get(position));
            }
            //holder.imageview.setImageBitmap(thumbnails.get(position));
            holder.checkbox.setChecked(thumbnailsselection.get(position));
            holder.id = position;
            return convertView;
        }

        class LoadImage extends AsyncTask<Object, Void, Bitmap> {

            private volatile Exception mError;
            private ImageView imv;
            private int position;
            private String path;

            public LoadImage(ImageView imv) {
                this.imv = imv;
                this.position = (Integer) imv.getTag();
                this.path = arrPath.get(this.position);
            }

            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap bitmap = null;
                File file = new File(this.path);

                try {
                    if (file.exists()) {
                        //MediaStore.Images.Thumbnails.getThumbnail
                        //bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        //bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(this.path),
                        //       THUMBSIZE, THUMBSIZE);
                        //thumbnails.set(position, bitmap);
                        thumbnails.set(position, ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(this.path),
                                THUMBSIZE, THUMBSIZE));
                    }
                } catch (Exception e) {
                    mError = e;
                    Log.e("Error", e.toString());
                    return null;
                }

                return thumbnails.get(position);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (!(arrPath.get((Integer) imv.getTag())).equals(path)) {
                    return;
                }

                if (result != null && imv != null) {
                    imv.setVisibility(View.VISIBLE);
                    imv.setImageBitmap(result);
                } else {
                    //imv.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.encryptBtn:
                if (thumbnailsselection == null)
                    return false;

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
                            //if (imageAdapter != null)
                            //    imageAdapter.remove(i);
                            //i--;
                        }
                    }
                }

                if (innames.size() > 0) {
                    Intent intent = new Intent(getApplicationContext(), CryptoUtility.class);
                    intent.putExtra(CryptoUtility.CIPHER_MODE, Cipher.ENCRYPT_MODE);
                    intent.putExtra(CryptoUtility.DELETE_AFTER_CIPHER, true);
                    intent.putExtra(CryptoUtility.IN_NAMES, innames);
                    intent.putExtra(CryptoUtility.TARGET_DIR_PATHS, targetPathDirs);
                    intent.putExtra(CryptoUtility.OUT_NAMES, outnames);
                    intent.putExtra(CryptoUtility.PENDING_DELETION_INT, pendingDeletionArr);
                    //startActivity(intent);
                    startActivityForResult(intent, 77);
                } else {
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                            "No file is selected.",
                            Snackbar.LENGTH_SHORT);
                    View v = snack.getView();
                    TextView tv = (TextView) v.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    snack.show();
                }
                return true;

            case R.id.deleteBtn:


                int deletecount = 0;
                if (thumbnailsselection == null)
                    return true;
                for (int i = 0; i < thumbnailsselection.size(); i++) {
                    boolean selected = thumbnailsselection.get(i);
                    if (selected) {
                        deletecount++;
                    }
                }

                //Do delete prompt
                if (deletecount > 0) {
                    Intent intent = new Intent(getApplicationContext(), DeleteUtility.class);
                    intent.putExtra(DeleteUtility.DELETE_COUNT_EXTRA, deletecount);
                    startActivityForResult(intent,DeleteUtility.DELETE_ACTIVITY_RQ_CODE);
                } else {
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content),
                            "No file is selected.",
                            Snackbar.LENGTH_SHORT);
                    View v = snack.getView();
                    TextView tv = (TextView) v.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    snack.show();
                }
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }
}