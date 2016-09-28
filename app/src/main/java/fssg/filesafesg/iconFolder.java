package fssg.filesafesg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

public class iconFolder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_folder);

        //toolbar session
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.icon_folder_title);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(R.string.title_activity_icon_folder);
        getSupportActionBar().setIcon(R.drawable.icon_icon);

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
                Toast.makeText(iconFolder.this, "option 1 click", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_2:
                Toast.makeText(iconFolder.this, "Return to Homepage", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(iconFolder.this, Homepage.class);
                startActivity(intent);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

}
