package fssg.filesafesg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.spec.KeySpec;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtility {


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
        cipher.init(ciphermode,secretkey);

/*
        //Simple secret key generation key
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec((password + salt).getBytes("UTF-8"), "AES");
        cipher.init(ciphermode,keySpec);
        */

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

}
