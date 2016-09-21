package fssg.filesafesg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class Homepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    public void photoFolder (View view){
        Intent intent = new Intent(this, photoFolder.class);
        startActivity(intent);
    }

    public void videoFolder (View view){
        Intent intent = new Intent(this, videoFolder.class);
        startActivity(intent);
    }

    public void docFolder (View view){
        Intent intent = new Intent(this, docFolder.class);
        startActivity(intent);
    }

    public void iconFolder (View view){
        Intent intent = new Intent(this, iconFolder.class);
        startActivity(intent);
    }



}
