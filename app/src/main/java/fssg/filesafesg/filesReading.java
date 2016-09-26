package fssg.filesafesg;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by purpl_000 on 26/9/2016.
 */

public class filesReading extends ListActivity {
    private File file;
    private List<String> myList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myList = new ArrayList<String>();

        String root_sd = Environment.getExternalStorageDirectory().toString();
        file = new File(root_sd + "/external_sd");
        File list[] = file.listFiles();

        for (int i = 0; i < list.length; i++) {
            myList.add(list[i].getName());
        }

        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, myList));

    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        File temp_file = new File(file, myList.get(position));

        if (!temp_file.isFile()) {
            file = new File(file, myList.get(position));
            File list[] = file.listFiles();

            myList.clear();

            for (int i = 0; i < list.length; i++) {
                myList.add(list[i].getName());
            }
            Toast.makeText(getApplicationContext(), file.toString(), Toast.LENGTH_LONG).show();
            setListAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, myList));

        }

    }


    @Override
    public void onBackPressed() {
        String parent = file.getParent().toString();
        file = new File(parent);
        File list[] = file.listFiles();

        myList.clear();

        for (int i = 0; i < list.length; i++) {
            myList.add(list[i].getName());
        }
        Toast.makeText(getApplicationContext(), parent, Toast.LENGTH_LONG).show();
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, myList));


    }

}