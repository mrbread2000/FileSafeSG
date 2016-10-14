package fssg.filesafesg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class docFolder extends AppCompatActivity implements View.OnClickListener {

    ImageButton button1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_folder);


        //toolbar session
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.doc_folder_title);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(R.string.title_activity_doc_folder);
        getSupportActionBar().setIcon(R.drawable.doc_icon);

        button1 = (ImageButton) findViewById(R.id.addPhotoFileB);
        button1.setOnClickListener(this); // calling onClick() method

    }

    //menu session
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_1:
                Toast.makeText(docFolder.this, "option 1 click", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_2:
                Toast.makeText(docFolder.this, "Return to Homepage", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(docFolder.this, Homepage.class);
                startActivity(intent);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.addPhotoFileB:
                Intent intent = new Intent(docFolder.this, filefolder.class);
                startActivity(intent);
                break;
        }


    }

}

