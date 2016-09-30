package fssg.filesafesg;

/**
 * Created by Kevin on 9/26/2016.
 */


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button photos, videos, docs, encryptfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar session
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        photos = (Button) findViewById(R.id.photos);
        videos = (Button) findViewById(R.id.videos);
        docs = (Button) findViewById(R.id.documents);

        photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Photos.class);
                startActivity(i);
            }
        });
        videos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Videos.class);
                startActivity(i);
            }
        });
        docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Documents.class);
                startActivity(i);
            }
        });


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
                Toast.makeText(MainActivity.this, "option 1 click", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_2:
                Toast.makeText(MainActivity.this, "Return to Homepage", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
