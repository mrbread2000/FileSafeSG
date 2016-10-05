package fssg.filesafesg;

/**
 * Created by Kevin on 9/26/2016.
 */


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button audio, photos, videos, docs, encryptfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audio = (Button) findViewById(R.id.audio);
        photos = (Button) findViewById(R.id.photos);
        videos = (Button) findViewById(R.id.videos);
        docs = (Button) findViewById(R.id.documents);
        encryptfiles = (Button) findViewById(R.id.encryptedfiles);

        photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PhotoFolder.class);
                startActivity(i);
            }
        });
        videos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), VideoFolder.class);
                startActivity(i);
            }
        });
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AudioFolder.class);
                startActivity(i);
            }
        });
        docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DocumentFolder.class);
                startActivity(i);
            }
        });
        encryptfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EncryptionClass.class);
                startActivity(i);
            }
        });
    }
}
