package net.nickcode4fun.securedemo.biometric;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.biometric.BiometricManager;

public class BiometricWrapper {
    private Context context;
    private BiometricManager biometricManager;

    public BiometricWrapper(Context context) {
        this.context = context;
        biometricManager = BiometricManager.from(context);
    }

    /**
     * 硬體是否支援
     */
    public boolean isHardwareDetected() {
        return biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ||
                biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE;
    }

    /**
     * 是否支援指紋辨識
     * */
    public boolean isSupportFingerprint() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
    }

    /**
     * 是否有生物驗證可以使用
     * */
    public boolean canAuthenticate() {
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(this.getClass().getName(), "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(this.getClass().getName(), "No biometric features available on this device.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(this.getClass().getName(), "Biometric features are currently unavailable.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(this.getClass().getName(), "The user hasn't associated any biometric credentials with their account.");
                return false;
            default:
                return false;
        }
    }
}
