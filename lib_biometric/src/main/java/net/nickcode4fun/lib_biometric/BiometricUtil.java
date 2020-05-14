package net.nickcode4fun.lib_biometric;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

import static android.content.ContentValues.TAG;

public class BiometricUtil {
    public BiometricPrompt createBiometricPrompt(FragmentActivity context) {
        Executor executor = ContextCompat.getMainExecutor(context);
        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.d(TAG, "#### errorCode: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Authentication was successful");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "#### Authentication failed for an unknown reason");
            }
        };
        return new BiometricPrompt(context, executor, callback);
    }

    public BiometricPrompt.PromptInfo createPromptInfo() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("生物辨識喔")
                .setSubtitle("這是子標題")
                .setDescription("勸你還是完成一下生物辨識")
                // Authenticate without requiring the user to press a "confirm"
                // button after satisfying the biometric check
                .setConfirmationRequired(false)
                .setNegativeButtonText("關閉")
                .build();
    }
}
