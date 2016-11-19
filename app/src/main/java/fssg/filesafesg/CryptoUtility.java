package fssg.filesafesg;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
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
    public static final String PENDING_DELETION_INT = "pendingdeletionint";

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
    private ArrayList<Integer> pendingDeletionArr;
    private String password = "defaultpasswordbad";

    private boolean isCrypting = false;
    private boolean stopPrompted = false;
    private int totalFileSize;

    private Activity thisActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);
        setTitle(R.string.title_dialog_crypto);

        //get data
        Bundle extras = getIntent().getExtras();
        cipherMode = extras.getInt(CryptoUtility.CIPHER_MODE);
        readAfterCipher = extras.getBoolean(CryptoUtility.READ_AFTER_CIPHER);
        deleteAfterCipher = extras.getBoolean(CryptoUtility.DELETE_AFTER_CIPHER);
        inNames = extras.getStringArrayList(CryptoUtility.IN_NAMES);
        targetPathDirs = extras.getStringArrayList(CryptoUtility.TARGET_DIR_PATHS);
        outNames = extras.getStringArrayList(CryptoUtility.OUT_NAMES);
        pendingDeletionArr = extras.getIntegerArrayList(CryptoUtility.PENDING_DELETION_INT);

        //user interaction with outside touch
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);


        //Get total file size
        totalFileSize = 0;
        for (String str:inNames){
            File f = new File(str);
            totalFileSize += f.length() / 1024;
        }

        //Misc
        thisActivity = this;
        isCrypting = false;
        stopPrompted = false;
        SharedPreference.pendingDeletionIntArray.clear();
        SharedPreference.successfulFileCount = 0;

        //Layout Functions
        final EditText etPassword = (EditText) findViewById(R.id.crypto_password);
        final Button btOkBtn = (Button) findViewById(R.id.crypto_ok_button);
        final TextView txtView = (TextView) findViewById(R.id.cryTextView);
        final CheckBox cryCheckbox = (CheckBox) findViewById(R.id.cryCheckBox);
        final RelativeLayout relLayout = (RelativeLayout) findViewById(R.id.cryDeleteLayout);

        //hide if "deleteAfterCipher" is false originally
        if (deleteAfterCipher == false){
            cryCheckbox.setChecked(false);
            relLayout.setVisibility(View.GONE);
        } else {
            cryCheckbox.setChecked(true);
        }

        btOkBtn.setOnClickListener(new View.OnClickListener() {

            //OK Button
            public void onClick(View v) {

                if (isCrypting) {
                    stopPrompted = true;
                } else {
                    //Flag as encrypting
                    isCrypting = true;

                    //hide UI
                    etPassword.setVisibility(View.GONE);
                    relLayout.setVisibility(View.GONE);

                    //change text to encrypting/decrypting
                    if (cipherMode == Cipher.ENCRYPT_MODE){
                        txtView.setText("Encrypting...");
                    } else if (cipherMode == Cipher.DECRYPT_MODE){
                        txtView.setText("Decrypting...");
                    }

                    //Flag delete
                    deleteAfterCipher = cryCheckbox.isChecked();

                    //change to cancel
                    btOkBtn.setText("Stop");

                    //do encrypt/decrypt and show progress
                    password = etPassword.getText().toString();
                    new ProgressUIOperation().execute("");
                }

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isCrypting) {
            finish();
            return true;
        }

        //return super.onTouchEvent(event);
        return true;
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

                //reset successful file count
                SharedPreference.successfulFileCount = 0;

                //encrypt/decrypt file
                FileInputStream stream_in = null;
                FileOutputStream stream_out = null;
                for (int i = 0; i < inNames.size(); i++) {

                    stream_in = null;
                    stream_out = null;
                    try {
                        //for progress bar accuracy
                        int oldProgress = totalProgress;

                        //constructs variable
                        String inName = inNames.get(i);
                        String outName = outNames.get(i);
                        String targetPathDir = targetPathDirs.get(i);

                        //get file in
                        fileIn = new File(inName);

                        //get file out, but check if it's already exist
                        String tempName = outName;
                        boolean doneChecking = false;
                        int tempi = 1;
                        while (!doneChecking) {
                            fileOut = new File(targetPathDir, tempName);
                            if(fileOut.exists()) {
                                //seperate both then append with number in between
                                String extension = "";
                                if (outName.indexOf(".fsg") > 0) {
                                    String nofsgName = outName.replace(".fsg", "");
                                    extension = outName.substring(nofsgName.lastIndexOf("."));
                                    tempName = outName.substring(0, nofsgName.lastIndexOf("."));
                                    tempName = tempName + " (" + tempi + ")" + extension;
                                } else if (outName.indexOf(".") > 0){
                                    extension = outName.substring(outName.lastIndexOf("."));
                                    tempName = outName.substring(0, outName.lastIndexOf("."));
                                    tempName = tempName + " (" + tempi + ")" + extension;
                                }


                                //add counter
                                tempi++;
                            }else{
                                doneChecking = true;
                            }
                        }
                        Log.d("FinalOut", tempName);

                        stream_in = new FileInputStream(fileIn);
                        stream_out = new FileOutputStream(fileOut);
                        int blockSize = cipher.getBlockSize();
                        int outputSize = cipher.getOutputSize(blockSize) + 1;
                        byte[] byte_in = new byte[blockSize * 1024];
                        byte[] byte_out = new byte[outputSize * 1024];
                        //stream_in.read(byte_in);

                        //create directory
                        File dr = new File(targetPathDir);
                        if (!dr.exists()) {
                            dr.mkdirs();
                        }

                        //here, we will loop so that we can show progress
                        BufferedInputStream inStream = new BufferedInputStream(stream_in);
                        int inLength = 0;
                        boolean more = true;
                        while (more) {
                            inLength = inStream.read(byte_in);
                            if (inLength/1024 == blockSize)
                            {
                                int outLength
                                        = cipher.update(byte_in, 0, blockSize*1024, byte_out);
                                stream_out.write(byte_out, 0, outLength);

                            }
                            else more = false;

                            //update progress bar
                            totalProgress += inLength / 1024;
                            publishProgress(totalProgress * 100 / totalFileSize);

                            //If stop prompted, stop crypting
                            if (stopPrompted) {
                                success = false;
                                break;
                            }
                        }

                        //Write out
                        stream_in.close();
                        if (success){
                            if (inLength > 0)
                                byte_out = cipher.doFinal(byte_in, 0, inLength);
                            else
                                byte_out = cipher.doFinal();
                            stream_out.write(byte_out);
                            stream_out.close();
                        } else {
                            stream_out.close();
                            return "failed";
                        }

                        SharedPreference.successfulFileCount++;
                        //Update the android cache
                        if (!readAfterCipher) {
                            MediaScanner.scanMedia(fileOut.getAbsolutePath(), thisActivity);
                        } else {
                            SharedPreference.targetCacheToClearDir = fileOut.getAbsolutePath();
                        }

                        //Delete file afterward
                        if (deleteAfterCipher) {
                            //delete file physically
                            if (fileIn != null && fileIn.exists())
                                fileIn.delete();

                            //delete cache file
                            MediaScanner.deleteMedia(fileIn.getAbsolutePath(), thisActivity);

                            //add to pending deletion list for other activity
                            SharedPreference.pendingDeletionIntArray.add(pendingDeletionArr.get(i));
                        }

                    } catch (BadPaddingException e){
                        //wrong password, close stream and delete failed decrypted file
                        if (stream_in != null)
                            stream_in.close();
                        if (stream_out != null)
                            stream_out.close();
                        if (fileOut != null)
                            fileOut.delete();
                        Log.e("CryptoError", "Bad Padding: " + e.toString());
                        success = false;
                    } catch (Exception e){
                        if (stream_in != null)
                            stream_in.close();
                        if (stream_out != null)
                            stream_out.close();
                        if (fileOut != null)
                            fileOut.delete();
                        Log.e("CryptoError", "Unknown Exception: " + e.toString());
                        success = false;
                    }
                }

            } catch (Exception e){
                success = false;
                if (fileOut != null)
                    fileOut.delete();
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
                if (stopPrompted)
                    setResult(RESULT_CANCELED, intent);
                else
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
