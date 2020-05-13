package net.nickcode4fun.securedemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import net.nickcode4fun.lib_local_storage.BaseSharedPreferences;
import net.nickcode4fun.securedemo.biometric.BiometricWrapper;
import net.nickcode4fun.securedemo.databinding.ActivityMainBinding;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

public class MainActivity extends AppCompatActivity {

    private static final String LOCAL_STORAGE_FILE_NAME = "Fugle";
    private static final String CHARSET = "ISO8859-1";
    private BiometricWrapper biometricWrapper;
    private ActivityMainBinding viewBinding;
    private KeyStore keyStore;
    private KeyUtil keyUtil;
    private BaseSharedPreferences localStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        localStorage = new BaseSharedPreferences(this, LOCAL_STORAGE_FILE_NAME);
        initKeystore();
        biometricWrapper = new BiometricWrapper(this);
        updateDeviceInfoView();
        updateKeystoreInfoView();
    }

    @SuppressLint("SetTextI18n")
    private void updateKeystoreInfoView() {
        try {
            viewBinding.txtAesSupport.setText("AES: " + (localStorage.containsKey("AES") ? "ON" : "OFF"));
            viewBinding.txtRsaSupport.setText("RSA: " + (keyStore.containsAlias(KeyUtil.KEYSTORE_ALIAS) ? "ON" : "OFF"));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    private void updateDeviceInfoView() {
        viewBinding.txtDeviceInfo.setText(getDeviceInfo(this));
    }

    private String getDeviceInfo(Context context) {
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;
        String softwareVersionName = Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
        boolean supportBiometric = biometricWrapper.canAuthenticate();
        boolean supportFingerprint = biometricWrapper.isSupportFingerprint();
        return "版本資訊\n" +
                "手機: " + deviceName + "\n" +
                "軟體版本: " + softwareVersionName + "\n" +
                "是否支援指紋辨識: " + supportBiometric + "\n" +
                "是否支援指紋辨識: " + supportFingerprint;
    }

    private void initKeystore() {
        try {
            keyStore = KeyStore.getInstance(KeyUtil.KEYSTORE_PROVIDER);
            keyStore.load(null);
            keyUtil = new KeyUtil(keyStore);
            // RSA
            if (!keyStore.containsAlias(KeyUtil.KEYSTORE_ALIAS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    keyUtil.generateRSAKey();
                else
                    keyUtil.generateRSAKey(this);

                Log.d(getClass().getName(), "#### Generate RSA key. alias: " + KeyUtil.KEYSTORE_ALIAS);
            } else {
                Log.d(getClass().getName(), "#### Already have RSA key. alias: " + KeyUtil.KEYSTORE_ALIAS);
            }
            // AES
            if (!localStorage.containsKey("AES")) {
                byte[] aesKey = keyUtil.generateAESKey();
                String encryption = keyUtil.encryptRSA(aesKey);
                localStorage.put("AES", encryption);
            }
            if (!localStorage.containsKey("IV")) {
                byte[] iv =keyUtil.generateIV();
                String encryption = keyUtil.encryptRSA(iv);
                localStorage.put("IV", encryption);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void test() {
        try {
            // AES
            if (!localStorage.containsKey("AES")) {
                byte[] aesKey = keyUtil.generateAESKey();
                String encryption = keyUtil.encryptRSA(aesKey);
                localStorage.put("AES", encryption);
            }
            if (!localStorage.containsKey("IV")) {
                byte[] iv =keyUtil.generateIV();
                String encryption = keyUtil.encryptRSA(iv);
                localStorage.put("IV", encryption);
            }
            byte[] aesKey = keyUtil.decryptRSA(localStorage.get("AES", ""));
            byte[] iv = keyUtil.decryptRSA(localStorage.get("IV", ""));

            Log.d(getClass().getName(), "#### AES key: " + new String(aesKey, CHARSET));
            Log.d(getClass().getName(), "#### IV: " + new String(iv, CHARSET));
            Log.d(getClass().getName(), "#### plainText: 哈哈哈哈哈");
            byte[] encryptionAES = keyUtil.encryptAES("哈哈哈哈哈".getBytes(), aesKey, iv);
            Log.d(getClass().getName(), "#### AES 加密: " + new String(encryptionAES, CHARSET));
            Log.d(getClass().getName(), "#### AES 解密: " + new String(keyUtil.decryptAES(encryptionAES, aesKey, iv)));
            ///
            Log.d(getClass().getName(), "################");

            String encryption = keyUtil.encryptRSA("哈哈哈哈哈".getBytes());
            Log.d(getClass().getName(), "#### 測試開始");
            Log.d(getClass().getName(), "#### plainText: " + "哈哈哈哈哈");
            Log.d(getClass().getName(), "#### RSA 加密: " + encryption);
            Log.d(getClass().getName(), "#### RSA 解密: " + new String(keyUtil.decryptRSA(encryption)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
