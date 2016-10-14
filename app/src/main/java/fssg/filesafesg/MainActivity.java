package fssg.filesafesg;

/**
 * Created by Kevin on 9/26/2016.
 */


import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity  {
    Button audio, photos, videos, docs, encryptfiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.onPause();


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
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
               // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("EXIT", true);
                if (getIntent().getBooleanExtra("EXIT", false)) {
                    finish();
                }


                startActivity(i);
            }
        });
        videos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), VideoFolder.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                i.putExtra("EXIT", true);
                if (getIntent().getBooleanExtra("EXIT", false)) {
                    finish();
                }

                startActivity(i);
            }
        });
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AudioFolder.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                i.putExtra("EXIT", true);
                if (getIntent().getBooleanExtra("EXIT", false)) {
                    finish();
                }


                startActivity(i);
            }
        });
        docs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DocumentFolder.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
               // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                i.putExtra("EXIT", true);
                if (getIntent().getBooleanExtra("EXIT", false)) {
                    finish();
                }



            }
        });
        encryptfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EncryptionClass.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
               // i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                i.putExtra("EXIT", true);
                if (getIntent().getBooleanExtra("EXIT", false)) {
                    finish();
                }

            }
        });

    }
}
