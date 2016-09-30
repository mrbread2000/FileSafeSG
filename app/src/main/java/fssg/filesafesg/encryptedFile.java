package fssg.filesafesg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class encryptedFile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_folder);

        //toolbar session
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.icon_folder_title);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_encrypted_folder);

    }

    //menu session
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

                switch (item.getItemId()){

                    case R.id.menu_1:
                        Toast.makeText(encryptedFile.this, "option 1 click", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.menu_2:
                        Toast.makeText(encryptedFile.this, "Return to Homepage", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(encryptedFile.this, Homepage.class);
                        startActivity(intent);
                        break;
                }

        return super.onOptionsItemSelected(item);
    }

}
