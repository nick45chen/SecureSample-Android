package net.nickcode4fun.securedemo;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.databinding.DataBindingUtil;

import net.nickcode4fun.lib_biometric.BiometricUtil;
import net.nickcode4fun.lib_local_storage.BaseSharedPreferences;
import net.nickcode4fun.securedemo.biometric.BiometricWrapper;
import net.nickcode4fun.securedemo.databinding.ActivityMainBinding;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.crypto.NoSuchPaddingException;

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
        setUpRSAEncryptBtn();
        setUpRSADecryptBtn();
        setUpAESEncryptBtn();
        setUpAESDecryptBtn();
        setUpTextContentClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_bio_auth: {
                // 生物辨識登入
                BiometricUtil biometricUtil = new BiometricUtil();
                BiometricPrompt biometricPrompt = biometricUtil.createBiometricPrompt(this);
                BiometricPrompt.PromptInfo promptInfo = biometricUtil.createPromptInfo();
                if (BiometricManager.from(this).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                    try {
                        //biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(keyUtil.getCipher()));
                        biometricPrompt.authenticate(promptInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpTextContentClick() {
        viewBinding.txtContent.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", viewBinding.txtContent.getText());
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "Copied Text", Toast.LENGTH_SHORT).show();
            return true;
        });
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

    @SuppressLint("SetTextI18n")
    private void setUpRSAEncryptBtn() {
        viewBinding.btnEncryptRsa.setOnClickListener(v -> {
            String content = viewBinding.editEncryption.getText() != null ? viewBinding.editEncryption.getText().toString() : "";
            if (TextUtils.isEmpty(content)) {
                viewBinding.txtContent.setText("");
                return;
            }
            try {
                String encryption = keyUtil.encryptRSA(content.getBytes());
                viewBinding.txtContent.setText(encryption);

            } catch (Exception e) {
                e.printStackTrace();
                viewBinding.txtContent.setText("RSA加密失敗: " + e.getMessage());
            } finally {
                hideKeyboard(this);
            }

        });
    }

    @SuppressLint("SetTextI18n")
    private void setUpRSADecryptBtn() {
        viewBinding.btnDecryptRsa.setOnClickListener(v -> {
            String content = viewBinding.editDecryption.getText() != null ? viewBinding.editDecryption.getText().toString() : "";
            if (TextUtils.isEmpty(content)) {
                viewBinding.txtContent.setText("");
                return;
            }
            try {
                String decryption = new String(keyUtil.decryptRSA(content));
                viewBinding.txtContent.setText(decryption);

            } catch (Exception e) {
                e.printStackTrace();
                viewBinding.txtContent.setText("RSA解密失敗: " + e.getMessage());
            } finally {
                hideKeyboard(this);
            }

        });
    }

    @SuppressLint("SetTextI18n")
    private void setUpAESEncryptBtn() {
        viewBinding.btnEncryptAes.setOnClickListener(v -> {
            String content = viewBinding.editEncryption.getText() != null ? viewBinding.editEncryption.getText().toString() : "";
            if (TextUtils.isEmpty(content)) {
                viewBinding.txtContent.setText("");
                return;
            }
            try {
                byte[] aesKey = keyUtil.decryptRSA(localStorage.get("AES", ""));
                byte[] iv = keyUtil.decryptRSA(localStorage.get("IV", ""));
                String encryption = new String(keyUtil.encryptAES(content.getBytes(), aesKey, iv), CHARSET);
                viewBinding.txtContent.setText(encryption);

            } catch (Exception e) {
                e.printStackTrace();
                viewBinding.txtContent.setText("AES加密失敗: " + e.getMessage());
            } finally {
                hideKeyboard(this);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setUpAESDecryptBtn() {
        viewBinding.btnDecryptAes.setOnClickListener(v -> {
            String content = viewBinding.editDecryption.getText() != null ? viewBinding.editDecryption.getText().toString() : "";
            if (TextUtils.isEmpty(content)) {
                viewBinding.txtContent.setText("");
                return;
            }
            try {
                byte[] aesKey = keyUtil.decryptRSA(localStorage.get("AES", ""));
                byte[] iv = keyUtil.decryptRSA(localStorage.get("IV", ""));
                String decryption = new String(keyUtil.decryptAES(content.getBytes(CHARSET), aesKey, iv));
                viewBinding.txtContent.setText(decryption);

            } catch (Exception e) {
                e.printStackTrace();
                viewBinding.txtContent.setText("AES解密失敗: " + e.getMessage());
            } finally {
                hideKeyboard(this);
            }
        });
    }

    private String getDeviceInfo(Context context) {
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;
        String softwareVersionName = Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
        boolean supportBiometric = biometricWrapper.isHardwareDetected();
        boolean supportFingerprint = biometricWrapper.isSupportFingerprint();
        return "版本資訊\n" +
                "手機: " + deviceName + "\n" +
                "軟體版本: " + softwareVersionName + "\n" +
                "是否支援生物辨識: " + supportBiometric + "\n" +
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
                byte[] iv = keyUtil.generateIV();
                String encryption = keyUtil.encryptRSA(iv);
                localStorage.put("IV", encryption);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(AppCompatActivity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                byte[] iv = keyUtil.generateIV();
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
