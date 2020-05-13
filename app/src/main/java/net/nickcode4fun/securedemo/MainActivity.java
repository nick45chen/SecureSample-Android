package net.nickcode4fun.securedemo;

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
        try {
            if (!localStorage.containsKey("AES"))
                localStorage.put("AES", new String(keyUtil.generateAESKey(), "ISO8859-1"));
            if (!localStorage.containsKey("IV"))
                localStorage.put("IV", new String(keyUtil.generateIV(), "ISO8859-1"));
            byte[] aeskey = localStorage.get("AES", "").toString().getBytes("ISO8859-1");
            byte[] iv = localStorage.get("IV", "").toString().getBytes("ISO8859-1");
            Log.d(getClass().getName(), "#### AES key: " + new String(aeskey, "ISO8859-1"));
            Log.d(getClass().getName(), "#### IV: " + new String(iv, "ISO8859-1"));
            Log.d(getClass().getName(), "#### plainText: 哈哈哈哈哈");
            byte[] encryptionAES = keyUtil.encryptAES("哈哈哈哈哈".getBytes(), aeskey, iv);
            Log.d(getClass().getName(), "#### AES 加密: " + new String(encryptionAES, "ISO8859-1"));
            Log.d(getClass().getName(), "#### AES 解密: " + new String(keyUtil.decryptAES(encryptionAES, aeskey, iv)));
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
            if (!keyStore.containsAlias(KeyUtil.KEYSTORE_ALIAS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    keyUtil.generateRSAKey();
                else
                    keyUtil.generateRSAKey(this);

                Log.d(getClass().getName(), "#### Generate RSA key. alias: " + KeyUtil.KEYSTORE_ALIAS);
            } else {
                Log.d(getClass().getName(), "#### Already have RSA key. alias: " + KeyUtil.KEYSTORE_ALIAS);
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
}
