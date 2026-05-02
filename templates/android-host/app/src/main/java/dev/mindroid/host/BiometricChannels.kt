package dev.mindroid.host

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricChannels(private val context: Context) : BridgeChannel {
    override val name: String = "biometrics"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "canAuthenticate" -> {
                val manager = BiometricManager.from(context)
                val code = manager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                mapOf(
                    "available" to (code == BiometricManager.BIOMETRIC_SUCCESS),
                    "code" to code
                )
            }

            "authenticate" -> {
                val activity = context as? FragmentActivity
                    ?: return mapOf("error" to "activity-required")

                val title = args?.get("title") as? String ?: "Verify your identity"
                val subtitle = args?.get("subtitle") as? String ?: ""
                val cancelLabel = args?.get("cancelLabel") as? String ?: "Cancel"

                authenticate(activity, title, subtitle, cancelLabel)
            }

            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        cancelLabel: String
    ): Any {
        var result: Map<String, Any?> = mapOf("pending" to true)

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(authResult: BiometricPrompt.AuthenticationResult) {
                result = mapOf("ok" to true, "type" to authResult.authenticationType)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                result = mapOf("error" to errString.toString(), "code" to errorCode)
            }

            override fun onAuthenticationFailed() {
                result = mapOf("error" to "authentication-failed")
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(cancelLabel)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        prompt.authenticate(promptInfo)
        return result
    }
}
