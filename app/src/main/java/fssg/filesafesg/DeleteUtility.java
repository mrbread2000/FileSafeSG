/**
 * Group: SS16/3C
 * Title: Secure File Folder in Android/iOS
 * File: AudioFolder.java
 */

package fssg.filesafesg;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.spec.KeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DeleteUtility extends Activity {

    public static final int DELETE_ACTIVITY_RQ_CODE = 33;
    public static final String DELETE_COUNT_EXTRA = "deletecount";

    public static final int DELETE_YES = -101;
    public static final int DELETE_NO = -100;



    private Activity thisActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        setTitle("Delete Confirmation");

        //get data
        Bundle extras = getIntent().getExtras();
        int i = extras.getInt(DeleteUtility.DELETE_COUNT_EXTRA);

        //user interaction with outside touch
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        //Misc
        thisActivity = this;

        //Layout Functions
        final Button btOkBtn = (Button) findViewById(R.id.del_ok_button);
        final Button btCancelBtn = (Button) findViewById(R.id.del_cancel_button);
        final TextView txtView = (TextView) findViewById(R.id.delText);

        txtView.setText("Delete " + Integer.toString(i) + " file(s)?");

        btOkBtn.setOnClickListener(new View.OnClickListener() {

            //OK Button
            public void onClick(View v) {

                Intent intent = new Intent();

                setResult(DELETE_YES, intent);
                thisActivity.finish();
                thisActivity = null;

            }
        });

        btCancelBtn.setOnClickListener(new View.OnClickListener() {

            //Cancel Button
            public void onClick(View v) {

                Intent intent = new Intent();

                setResult(DELETE_NO, intent);
                thisActivity.finish();
                thisActivity = null;

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //return super.onTouchEvent(event);
        return true;
    }


}
