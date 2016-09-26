package fssg.filesafesg;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class Homepage extends AppCompatActivity implements View.OnClickListener {

    Button button1, button2, button3, button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        button1 = (Button) findViewById(R.id.addPhotoButton);
        button1.setOnClickListener(this); // calling onClick() method
        button2 = (Button) findViewById(R.id.addVideoButton);
        button2.setOnClickListener(this); // calling onClick() method
        button3 = (Button) findViewById(R.id.addIconButton);
        button3.setOnClickListener(this); // calling onClick() method
        button4 = (Button) findViewById(R.id.addDocButton);
        button4.setOnClickListener(this); // calling onClick() method

    }


    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.addPhotoButton:
                Intent intent = new Intent(Homepage.this, photoFolder.class);
                startActivity(intent);
                break;
            case R.id.addVideoButton:
                Intent intent1 = new Intent(Homepage.this, videoFolder.class);
                startActivity(intent1);
                break;
            case R.id.addIconButton:
                Intent intent2 = new Intent(Homepage.this, iconFolder.class);
                startActivity(intent2);
                break;
            case R.id.addDocButton:
                Intent intent3 = new Intent(Homepage.this, docFolder.class);
                startActivity(intent3);
                break;
        }


    }

}