package it.unibo.gamelibrary.ui.views.settings

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.datastore.GenericPreferenceDataStoreSettingValueState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import it.unibo.gamelibrary.MainActivity
import it.unibo.gamelibrary.Secrets
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.interfaces.HasBiometrics
import it.unibo.gamelibrary.ui.destinations.LoginPageDestination
import it.unibo.gamelibrary.utils.findActivity
import it.unibo.gamelibrary.utils.snackBarHostState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserRepository,
    private val application: Application
) : ViewModel(), HasBiometrics {
    val auth: FirebaseAuth = Firebase.auth
    val settingsList = SettingsEnum.entries

    //Change email
    var openEmailDialog by mutableStateOf(false)
    var emailValue by mutableStateOf("")
    var isSignInWithGoogle by mutableStateOf(false)
    var passwordValue by mutableStateOf("")
    var isHidden by mutableStateOf(true)

    //Change password
    var openPasswordDialog by mutableStateOf(false)
    val passwordFields = mutableStateMapOf(
        Pair("old", ""),
        Pair("new", ""),
        Pair("confirm", ""),
    )
    val passwordsHidden = mutableStateMapOf(
        Pair("old", true),
        Pair("new", true),
        Pair("confirm", true),
    )

    //Change location
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var openLocationDialog by mutableStateOf(false)
    var addressUser by mutableStateOf<String?>(null)

    //Logout
    var openLogoutDialog by mutableStateOf(false)

    fun changeEmail(result: ActivityResult, context: Context) {
        if ("^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$".toRegex()
                .matches(emailValue)
        ) {
            if (result.resultCode == RESULT_OK) {
                if (isSignInWithGoogle) {
                    val oneTapClient = Identity.getSignInClient(context)
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    Log.i("Token", idToken.toString())
                    if (idToken != null) {
                        val credentials = GoogleAuthProvider.getCredential(idToken, null)
                        reAuthenticateAndChange(
                            credentials,
                            emailValue,
                            "Impossible to change the email. Please re-sign in"
                        ) {
                            viewModelScope.launch {
                                auth.currentUser?.updateEmail(it)
                                repository.setEmail(auth.currentUser?.uid!!, it)
                            }
                        }
                    } else {
                        viewModelScope.launch {
                            snackBarHostState.showSnackbar("Impossible to change the email")
                        }
                    }
                } else {
                    val credentials =
                        EmailAuthProvider.getCredential(auth.currentUser?.email!!, passwordValue)
                    reAuthenticateAndChange(
                        credentials,
                        emailValue,
                        "Impossible to change the email. Password is incorrect. Please re-sign in"
                    ) {
                        viewModelScope.launch {
                            auth.currentUser?.updateEmail(it)
                            repository.setEmail(auth.currentUser?.uid!!, it)
                        }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                snackBarHostState.showSnackbar("Impossible to change the email. Invalid email")
            }
        }
        openEmailDialog = false
        emailValue = ""
        passwordValue = ""
    }

    fun changePassword(result: ActivityResult, context: Context) {
        if (passwordFields["new"] == passwordFields["confirm"]) {
            if (result.resultCode == RESULT_OK) {
                if (isSignInWithGoogle) {
                    val oneTapClient = Identity.getSignInClient(context)
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    Log.i("Token", idToken.toString())
                    if (idToken != null) {
                        val credentials = GoogleAuthProvider.getCredential(idToken, null)
                        reAuthenticateAndChange(
                            credentials,
                            passwordFields["new"]!!,
                            "Impossible to change the password"
                        ) {
                            auth.currentUser?.updatePassword(it)
                        }
                    } else {
                        viewModelScope.launch {
                            snackBarHostState.showSnackbar("Impossible to change the password")
                        }
                    }
                } else {
                    val credentials =
                        EmailAuthProvider.getCredential(
                            auth.currentUser?.email!!,
                            passwordFields["old"]!!
                        )
                    reAuthenticateAndChange(
                        credentials,
                        passwordFields["new"]!!,
                        "Impossible to change the password. Old password is incorrect"
                    ) {
                        auth.currentUser?.updatePassword(it)
                    }
                }
            }
        } else {
            viewModelScope.launch {
                snackBarHostState.showSnackbar("Impossible to change the password. New password and Confirm password are different")
            }
        }
        for (key in passwordFields.keys) {
            passwordFields[key] = ""
        }
        openPasswordDialog = false
    }

    suspend fun launchChange(
        context: Context,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        Log.i("Provider", auth.currentUser?.providerData?.size.toString())
        val oneTapClient = Identity.getSignInClient(context)
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(Secrets().getGoogleServerClientId(context.packageName))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()
        try {
            val result = oneTapClient.beginSignIn(signInRequest).await()
            // Now construct the IntentSenderRequest the launcher requires
            val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
            launcher.launch(intentSenderRequest)
        } catch (e: Exception) {
            // No saved credentials found.Launch the One Tap sign-up flow, or
            // do nothing and continue presenting the signed-out UI.
            Log.i("LOG", e.message.toString())
        }
    }

    fun getCurrentPosition(@ActivityContext activityContext: Context) {
        val activity = activityContext.findActivity() as MainActivity
        if (ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(activityContext, "Permissions denied", Toast.LENGTH_SHORT).show()
            return
        }
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(activity, "Cannot get location.", Toast.LENGTH_SHORT).show()
            } else {
                viewModelScope.launch {
                    repository.setLocation(
                        auth.currentUser?.uid!!,
                        "${location.latitude} ${location.longitude}"
                    )
                    showAddress(activityContext, location.latitude, location.longitude)
                    getAddress()
                }
            }
        }
        openLocationDialog = false
    }

    fun logout(navController: NavController) {
        auth.signOut()
        navController.navigate(LoginPageDestination())
    }

    fun openMap(latitude: Double, longitude: Double, context: Context) {
        val uri = "geo:$latitude,$longitude?q=$latitude,$longitude"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    fun getAddress() {
        viewModelScope.launch {
            addressUser = repository.getUserByUid(auth.currentUser?.uid!!).first()?.address
        }
    }

    private fun showAddress(
        @ApplicationContext context: Context,
        latitude: Double,
        longitude: Double
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            Geocoder(
                context,
                Locale.getDefault()
            ).getFromLocation(
                latitude,
                longitude,
                1
            ) { addresses ->
                if (addresses[0] != null) {
                    Log.d("ADDRESS", addresses[0].toString())
                    viewModelScope.launch {
                        snackBarHostState.showSnackbar(
                            "Address changed. Your new address is ${
                                addresses[0].getAddressLine(
                                    0
                                )
                            }"
                        )
                    }
                } else {
                    viewModelScope.launch {
                        snackBarHostState.showSnackbar("Impossible change address")
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val address = Geocoder(
                context,
                Locale.getDefault()
            ).getFromLocation(
                latitude,
                longitude,
                1
            )?.get(0)
            if (address != null) {
                viewModelScope.launch {
                    snackBarHostState.showSnackbar(
                        "Address changed. Your new address is ${
                            address.getAddressLine(
                                0
                            )
                        }"
                    )
                }
            } else {
                viewModelScope.launch {
                    snackBarHostState.showSnackbar("Impossible change address")
                }
            }
        }
    }

    private fun reAuthenticateAndChange(
        credentials: AuthCredential,
        field: String,
        error: String,
        change: (field: String) -> Unit
    ) {
        auth.currentUser?.reauthenticate(credentials)
            ?.addOnCompleteListener { task ->
                Log.i("Authenticate", task.isSuccessful.toString())
                if (task.isSuccessful) {
                    change.invoke(field)
                } else {
                    viewModelScope.launch {
                        snackBarHostState.showSnackbar(error)
                    }
                }
            }
    }

    fun toggleBiometrics(
        context: Context,
        state: GenericPreferenceDataStoreSettingValueState<Boolean>
    ) {
        showBiometricPrompt(
            context,
            "${if (state.value) "Enable" else "Disable"} biometric protection",
            onError = { errorCode, errorMessage ->
                state.value = !state.value
                Toast.makeText(
                    context,
                    "Authentication Error ($errorCode): $errorMessage",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onCancel = {
                state.value = !state.value
                Toast.makeText(context, "Authentication Cancelled", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun goToNotificationSettings() {
        val context = application.applicationContext
        val intent = Intent()
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

