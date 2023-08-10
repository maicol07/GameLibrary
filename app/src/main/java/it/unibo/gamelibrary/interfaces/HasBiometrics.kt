package it.unibo.gamelibrary.interfaces

import android.content.Context
import android.os.CancellationSignal
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import it.unibo.gamelibrary.utils.findActivity

interface HasBiometrics {
    fun showBiometricPrompt(
        context: Context,
        title: String,
        onSuccess: (result: BiometricPrompt.AuthenticationResult?) -> Unit = {},
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onCancel: () -> Unit = {},
        onNegativeButtonClick: () -> Unit = onCancel,
        onFailed: () -> Unit = onCancel
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .apply {
                setTitle(title)
                setConfirmationRequired(false)
                setNegativeButtonText("Cancel")
            }.build()

        val authCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(context.findActivity(), ContextCompat.getMainExecutor(context), authCallback)

        val cancellationSignal = CancellationSignal()
        cancellationSignal.setOnCancelListener(onCancel)

        biometricPrompt.authenticate(promptInfo)
    }

    fun isBiometricAvailable(context: Context): Int = BiometricManager.from(context)
        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
}