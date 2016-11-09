package fssg.filesafesg;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;

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

    //used to pass value via intent/bundle's Extras
    public static final String CIPHER_MODE = "ciphermode";
    public static final String DELETE_AFTER_CIPHER = "deleteafterencrypt";
    public static final String IN_NAMES = "innames";
    public static final String OUT_NAMES = "outnames";
    public static final String TARGET_DIR_PATHS = "dirpaths";


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
    private ArrayList<String> targetPathDirs;
    private ArrayList<String> inNames;
    private ArrayList<String> outNames;

    private int totalFileSize;

    private Activity thisActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        //get data
        Bundle extras = getIntent().getExtras();
        cipherMode = extras.getInt(CryptoUtility.CIPHER_MODE);
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

        //do encrypt/decrypt and show progress
        new ProgressUIOperation().execute("");
    }


    // Progress Bar Class =========================================================================

    private class ProgressUIOperation extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                //initialize secret key and cipher method
                SecretKeyFactory skfactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec("password".toCharArray(), "salt".getBytes("UTF-8"), 5000, 256);
                SecretKey tmp = skfactory.generateSecret(spec);
                SecretKey secretkey = new SecretKeySpec(tmp.getEncoded(),"AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(cipherMode,secretkey, new IvParameterSpec(IV));

                //Also initialize total bytes to be processed for Progress Bar UI
                int totalProgress = 0;

                //encrypt/decrypt file
                for (int i = 0; i < inNames.size(); i++){

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

                    //here, we will loop so that we can show progress
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(cipher.getOutputSize(byte_in.length));
                    int offset = 0;
                    while(offset + 1024 < byte_in.length) {
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
                    MediaScanner.scanMedia(fileOut.getAbsolutePath(), thisActivity);

                    //Delete file afterward
                    if (deleteAfterCipher){
                        if (fileIn != null && fileIn.exists())
                            fileIn.delete();
                        MediaScanner.deleteMedia(fileIn.getAbsolutePath(), thisActivity);
                    }
                }

            } catch (Exception e){
                System.out.println("Error encrypting file:\n" + e);
            }


            return "Completed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //read file after decrypting
            //if (outNames.size() == 1){
            //    Utility.openFile(getApplicationContext(), fileOut);
            //}

            //close this dialog
            thisActivity.finish();
            thisActivity = null;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ProgressBar pb = (ProgressBar) findViewById(R.id.crypto_progressUI);
            pb.setProgress(values[0]);
        }
    }


}
