package net.nickcode4fun.securedemo;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import net.nickcode4fun.securedemo.biometric.BiometricWrapper;
import net.nickcode4fun.securedemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private BiometricWrapper biometricWrapper;
    private ActivityMainBinding viewBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        biometricWrapper = new BiometricWrapper(this);
        updateDeviceInfoView();
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


}
