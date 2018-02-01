package com.example.martinjonovski.chatnoir;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Martin Jonovski on 12/25/2017.
 */

public class RSAUtil {

    private Cipher cipher;

    public RSAUtil() throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
    }

    //
    public byte[] encrypt(PublicKey pubKey2, String input) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, pubKey2, new SecureRandom());
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return cipherText;
    }
//
//    public byte[] decrypt(Key privKey, byte[] cipherText) throws Exception {
//        cipher.init(Cipher.DECRYPT_MODE, privKey);
//        byte[] plainText = cipher.doFinal(cipherText);
//        return plainText;
//    }

    public String encryptString(String publicK, String initialText) throws Exception {
        byte[] publicBytes = Base64.decode(publicK, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        return new String(Base64.encode(encrypt(pubKey, initialText), Base64.DEFAULT));
    }

    public String decryptString(String alias, String encryptedText) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            output.init(Cipher.DECRYPT_MODE, privateKey);

            String cipherText = encryptedText;
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            return finalText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public String signString(String alias, byte[] encyptedText, PrivateKey privateKey) {
        try {
            Signature sig = Signature.getInstance("MD5WithRSA");
            sig.initSign(privateKey);
            sig.update(encyptedText);

            byte[] signature = sig.sign();
            return Base64.encodeToString(signature, Base64.DEFAULT);

        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public boolean verifySign(String data, String signature, String publicK) {
        try {
            byte[] publicBytes = Base64.decode(publicK, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Signature sig = Signature.getInstance("MD5WithRSA");
            sig.initVerify(pubKey);
            sig.update(data.getBytes());

            boolean kraj = sig.verify(Base64.decode(signature, Base64.DEFAULT));
            return kraj;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }


    public PrivateKey loadPrivateKey(String key64) throws GeneralSecurityException, IOException {
        byte[] clear = Base64.decode(key64.getBytes(), Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;

    }
}
