package fssg.filesafesg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.net.Uri;

import android.os.Environment;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import java.io.File;
import java.util.ArrayList;




/**
 * Created by purpl_000 on 19/9/2016.
 */
public class filefolder extends AppCompatActivity {
    GridView gv;
    ArrayList<File> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.file_folder);


       list = imageReader(Environment.getExternalStorageDirectory());


        gv = (GridView) findViewById(R.id.gridView);
        gv.setAdapter(new GridAdapter());

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view , int position, long id ){
                startActivity(new Intent(getApplicationContext(),ViewImage.class).putExtra("img",list.get(position).toString()));



            }
        });





    }

    class GridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

           convertView = getLayoutInflater().inflate(R.layout.single_grid, parent ,false);
           ImageView iv = (ImageView) convertView.findViewById(R.id.imageView);
            iv.setImageURI(Uri.parse(getItem(position).toString()));

            return convertView;
        }
    }

    ArrayList<File> imageReader(File root)
    {
        ArrayList<File> a = new ArrayList<>();
        File[] files = root.listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++)
                if (files[i].isDirectory()) {
                    a.addAll(imageReader(files[i]));


                } else {
                    if ((files[i].getName().endsWith(".jpg"))) {
                        a.add(files[i]);
                    }
                }
        }
     return a;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //inflate the menu , this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;

    }

}
