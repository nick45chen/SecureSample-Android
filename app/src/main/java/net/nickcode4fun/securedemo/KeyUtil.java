package net.nickcode4fun.securedemo;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;


public class KeyUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CIPHER_MODE = "AES/CBC/PKCS5Padding"; //algorithm/mode/padding
    public static final String KEYSTORE_ALIAS = "FugleAPPKey";
    public static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_CIPHER_MODE = "RSA/ECB/PKCS1Padding";

    private KeyStore keyStore;

    public KeyUtil(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public byte[] generateAESKey() {
        KeyGenerator keyGenerator;
        SecretKey secretKey = null;
        try {
            keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(256);
            secretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert secretKey != null;
        return secretKey.getEncoded();
    }

    public byte[] generateIV() {
        byte[] IV = new byte[16];
        SecureRandom random;
        random = new SecureRandom();
        random.nextBytes(IV);
        return IV;
    }

    public byte[] encryptAES(byte[] plaintext, byte[] key, byte[] IV) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER_MODE);
        SecretKeySpec keySpec = new SecretKeySpec(key, AES_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        //noinspection UnnecessaryLocalVariable
        byte[] cipherText = cipher.doFinal(plaintext);
        return cipherText;
    }

    public byte[] decryptAES(byte[] cipherText, byte[] key, byte[] IV) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CIPHER_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key, AES_ALGORITHM);

            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            //noinspection UnnecessaryLocalVariable
            byte[] decryptedText = cipher.doFinal(cipherText);
            return decryptedText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Android 6.0+ 生成 RSA key
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void generateRSAKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);
        KeyGenParameterSpec spec = new KeyGenParameterSpec
                .Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setCertificateSubject(new X500Principal("CN=" + KEYSTORE_ALIAS))
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512) //Set of digests algorithms with which the key can be used
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(false) //Sets whether this key is authorized to be used only if the user has been authenticated, default false
                .setUserAuthenticationValidityDurationSeconds(60)  //Duration(seconds) for which this key is authorized to be used after the user is successfully authenticated
                .setKeySize(2048) // RSA default key size is 2048
                .build();
        keyPairGenerator.initialize(spec);
        keyPairGenerator.generateKeyPair();
        Log.d(getClass().getName(), "#### generate RSA key. above 6.0+. api: " + Build.VERSION.SDK_INT);
    }

    /**
     * Android 6.0 以下 生成 RSA key
     */
    public void generateRSAKey(Context context) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEYSTORE_ALIAS)
                .setSubject(new X500Principal("CN=" + KEYSTORE_ALIAS))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM, KEYSTORE_PROVIDER);

        keyPairGenerator.initialize(spec);
        keyPairGenerator.generateKeyPair();
        Log.d(getClass().getName(), "#### generate RSA key. below 6.0. api " + Build.VERSION.SDK_INT);
    }

    public String encryptRSA(byte[] plainText) throws Exception {
        PublicKey publicKey = keyStore.getCertificate(KEYSTORE_ALIAS).getPublicKey();

        Cipher cipher = Cipher.getInstance(RSA_CIPHER_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedByte = cipher.doFinal(plainText);
        return Base64.encodeToString(encryptedByte, Base64.DEFAULT);
    }

    public byte[] decryptRSA(String encryptedText) throws Exception {
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(KEYSTORE_ALIAS, null);

        Cipher cipher = Cipher.getInstance(RSA_CIPHER_MODE);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT);
        //noinspection UnnecessaryLocalVariable
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return decryptedBytes;
    }
}
