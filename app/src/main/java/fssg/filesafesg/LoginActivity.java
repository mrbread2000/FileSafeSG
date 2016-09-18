package fssg.filesafesg;

import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button bLogin = (Button) findViewById(R.id.bLogin);
        final TextView registerLink = (TextView) findViewById(R.id.tvRegisterHere);

        //Link Register button to register page
        registerLink.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //Create an Intent that opens RegisterActivity
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                //Tell the current activity(LoginActivity to perform the intent(Opening of Register page)
                LoginActivity.this.startActivity(registerIntent);
            }
        });
    }
}
