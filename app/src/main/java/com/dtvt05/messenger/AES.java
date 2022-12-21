package com.dtvt05.messenger;

import android.os.Build;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
//import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
//    private String initVector;
//    private String key;

    public AES() {
    }


    public String encrypt(String plainText, String key, String initVector) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes());
                return Base64.encodeToString(cipherText,Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

//    public String decrypt(String encrypted, String) {
//        return this.decrypt(encrypted, this.key);
//    }

    public String decrypt(String cipherText, String key, String initVector) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] plainText =  cipher.doFinal(Base64.decode(cipherText,Base64.DEFAULT));
            return new String(plainText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}

