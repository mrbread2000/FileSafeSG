package fssg.filesafesg;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtility extends Activity {
    //public static final byte[] IV = { 65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45 };
    public static final byte[] IV = { 35, 7, 22, 26, 12, 22, 1, 4, 22, 55, 45, 63, 77, 32, 32, 23 };
    public static final String SALTY = "WHbIOOewmz0MRm4V";

    //used to pass value via intent/bundle's Extras
    public static final String CIPHER_MODE = "ciphermode";
    public static final String DELETE_AFTER_CIPHER = "deleteaftercipher";
    public static final String READ_AFTER_CIPHER = "readaftercipher";
    public static final String IN_NAMES = "innames";
    public static final String OUT_NAMES = "outnames";
    public static final String TARGET_DIR_PATHS = "dirpaths";

    public static final int CRYPTO_FAILED = -77;


    public static void encrypt(String password, String salt, File filein, File fileout) throws Exception{
        doCrypto(Cipher.ENCRYPT_MODE, password, salt, filein, fileout);
    }

    public static void decrypt(String password, String salt, File filein, File fileout) throws Exception{
        doCrypto(Cipher.DECRYPT_MODE, password, salt, filein, fileout);
    }

    private static void doCrypto(int ciphermode, String password, String salt, File filein, File fileout) throws Exception{


        //initialize secret key and cipher method
        SecretKeyFactory skfactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes("UTF-8"), 5000, 256);
        SecretKey tmp = skfactory.generateSecret(spec);
        SecretKey secretkey = new SecretKeySpec(tmp.getEncoded(),"AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(ciphermode,secretkey, new IvParameterSpec(IV));

        //encrypt/decrypt file
        FileInputStream stream_in = new FileInputStream(filein);
        byte[] byte_in = new byte[(int) filein.length()];
        stream_in.read(byte_in);

        byte[] byte_out = cipher.doFinal(byte_in);

        FileOutputStream stream_out = new FileOutputStream(fileout);
        stream_out.write(byte_out);

        stream_in.close();
        stream_out.close();
    }

    // new functions ===========================================================================================

    private int cipherMode;
    private File fileIn;
    private File fileOut;
    private boolean deleteAfterCipher;
    private boolean readAfterCipher;
    private ArrayList<String> targetPathDirs;
    private ArrayList<String> inNames;
    private ArrayList<String> outNames;
    private String password = "defaultpasswordbad";

    private int totalFileSize;

    private Activity thisActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        //get data
        Bundle extras = getIntent().getExtras();
        cipherMode = extras.getInt(CryptoUtility.CIPHER_MODE);
        readAfterCipher = extras.getBoolean(CryptoUtility.READ_AFTER_CIPHER);
        deleteAfterCipher = extras.getBoolean(CryptoUtility.DELETE_AFTER_CIPHER);
        inNames = extras.getStringArrayList(CryptoUtility.IN_NAMES);
        targetPathDirs = extras.getStringArrayList(CryptoUtility.TARGET_DIR_PATHS);
        outNames = extras.getStringArrayList(CryptoUtility.OUT_NAMES);

        //Get total file size
        totalFileSize = 0;
        for (String str:inNames){
            File f = new File(str);
            totalFileSize += f.length();
        }

        //Misc
        thisActivity = this;

        //Layout Functions
        final EditText etPassword = (EditText) findViewById(R.id.crypto_password);
        Button btOkBtn = (Button) findViewById(R.id.crypto_ok_button);

        btOkBtn.setOnClickListener(new View.OnClickListener() {

            //OK Button
            public void onClick(View v) {

                //do encrypt/decrypt and show progress
                password = etPassword.getText().toString();
                new ProgressUIOperation().execute("");

            }
        });
    }


    // Progress Bar Class =========================================================================

    private class ProgressUIOperation extends AsyncTask<String, Integer, String> {

        private boolean success = false;

        @Override
        protected String doInBackground(String... params) {

            success = true;
            try {
                //initialize secret key and cipher method
                SecretKeyFactory skfactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(password.toCharArray(), SALTY.getBytes("UTF-8"), 5000, 256);
                SecretKey tmp = skfactory.generateSecret(spec);
                SecretKey secretkey = new SecretKeySpec(tmp.getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(cipherMode, secretkey, new IvParameterSpec(IV));

                //Also initialize total bytes to be processed for Progress Bar UI
                int totalProgress = 0;

                //encrypt/decrypt file
                for (int i = 0; i < inNames.size(); i++) {

                    //for progress bar accuracy
                    int oldProgress = totalProgress;

                    //constructs variable
                    String inName = inNames.get(i);
                    String outName = outNames.get(i);
                    String targetPathDir = targetPathDirs.get(i);


                    fileIn = new File(inName);
                    fileOut = new File(targetPathDir, outName);

                    FileInputStream stream_in = new FileInputStream(fileIn);
                    byte[] byte_in = new byte[(int) fileIn.length()];
                    stream_in.read(byte_in);

                    //create directory
                    File dr = new File(targetPathDir);
                    if (!dr.exists()) {
                        dr.mkdirs();
                    }

                    //here, we will loop so that we can show progress
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(cipher.getOutputSize(byte_in.length));
                    int offset = 0;
                    while (offset + 1024 < byte_in.length) {
                        final byte[] cipherout = cipher.update(byte_in, offset, 1024);
                        //byte_out = Arrays.copyOf( byte_out, byte_out.length + 1024);
                        //System.arraycopy(cipherout, 0, byte_out, offset, 1024);
                        outputStream.write(cipherout);
                        offset += 1024;

                        //update progress bar
                        totalProgress += 1024;
                        publishProgress(totalProgress * 100 / totalFileSize);
                    }

                    //update progress bar
                    totalProgress = oldProgress + byte_in.length;
                    publishProgress(totalProgress * 100 / totalFileSize);

                    //Write out
                    outputStream.write(cipher.doFinal(byte_in, offset, byte_in.length - offset));
                    byte[] byte_out = outputStream.toByteArray();
                    outputStream.flush();

                    //Once done, wrap up everything
                    FileOutputStream stream_out = new FileOutputStream(fileOut);
                    stream_out.write(byte_out);

                    stream_in.close();
                    stream_out.close();

                    //Update the android cache
                    if (!readAfterCipher)
                        MediaScanner.scanMedia(fileOut.getAbsolutePath(), thisActivity);

                    //Delete file afterward
                    if (deleteAfterCipher) {
                        if (fileIn != null && fileIn.exists())
                            fileIn.delete();
                        MediaScanner.deleteMedia(fileIn.getAbsolutePath(), thisActivity);
                    }
                    Log.d("Crypto", fileOut.getAbsolutePath());
                }

            } catch (BadPaddingException e){
                success = false;
            } catch (Exception e){
                success = false;
                System.out.println("Error encrypting file:\n" + e);
            }

            return "Completed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Intent intent = new Intent();

            if (success){
                //read file after decrypting
                if (readAfterCipher){
                    Utility.openFile(getApplicationContext(), fileOut);
                }
                setResult(RESULT_OK, intent);
            } else {
                setResult(CRYPTO_FAILED, intent);
            }

            thisActivity.finish();
            thisActivity = null;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute();}

        private ProgressBar pb;
        @Override
        protected void onProgressUpdate(Integer... values) {
            if (pb == null)
                pb = (ProgressBar) findViewById(R.id.crypto_progressUI);
            pb.setProgress(values[0]);
        }

    }


}
