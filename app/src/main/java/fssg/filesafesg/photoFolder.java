package fssg.filesafesg;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class photoFolder extends AppCompatActivity implements View.OnClickListener {


    ImageButton button1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_folder);
            button1 = (ImageButton) findViewById(R.id.addPhotoFileB);
            button1.setOnClickListener(this); // calling onClick() method

        }


    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.addPhotoFileB:
                Intent intent = new Intent(photoFolder.this, filefolder.class);
                startActivity(intent);
                break;
        }


    }

}

