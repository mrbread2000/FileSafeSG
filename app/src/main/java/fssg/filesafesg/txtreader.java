package fssg.filesafesg;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

//import static fssg.filesafesg.R.id.load;

/**
 * Created by purpl_000 on 24/9/2016.
 */

public class txtreader extends AppCompatActivity {

    String filename = "sample.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readtxt);

        final EditText edtInput = (EditText) this.findViewById(R.id.tvTextInput);
        final TextView tvOutput = (TextView) this.findViewById(R.id.tvText);
        final Button save = (Button) this.findViewById(R.id.btnSave);
        final Button load = (Button) this.findViewById(R.id.btnLoad);

        final File file = new File(Environment.getExternalStorageDirectory(), filename);
        save.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {


                try {

                    FileOutputStream fo = new FileOutputStream(file);
                    fo.write(edtInput.getText().toString().getBytes());
                    fo.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        load.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                try {
                    int length  = (int )file.length();
                    byte[] bytes = new byte[length];

                    FileInputStream fi = new FileInputStream(file);
                    fi.read(bytes);
                    String text = new String(bytes);
                    tvOutput.setText(text);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                }catch(IOException E){
                    E.printStackTrace();
                }
            }
        });
    }
}