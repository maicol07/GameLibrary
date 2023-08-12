package it.unibo.gamelibrary.ui.views.Login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.Secrets
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    val fields = mutableStateMapOf(
        Pair("usernameOrEmail", ""),
        Pair("password", ""),
    )
    var emailResetPassword by mutableStateOf("")
    var openResetPasswordDialog by mutableStateOf(false)
    var isEmail by mutableStateOf(false)
    var isError by mutableStateOf(false)
    val isPasswordHidden = mutableStateOf(true)

    fun login(navController: NavController){
        viewModelScope.launch {
            var usernameOrEmail = fields["usernameOrEmail"]!!
            if (!isEmail) {
                val user = repository.getUserByUsername(usernameOrEmail)
                usernameOrEmail = user?.email ?: ""
                Log.i("Email From Username", usernameOrEmail)
            }
            Log.i("Email", usernameOrEmail)
            if (usernameOrEmail.isNotEmpty() && fields["password"]!!.isNotEmpty()) {
                auth.signInWithEmailAndPassword(usernameOrEmail, fields["password"]!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val displayName = auth.currentUser?.displayName?.split(" ")
                            val name = displayName?.get(0) ?: ""
                            val surname = displayName?.get(1) ?: ""
                            insertUserIfNotExist(name, surname, "${name}_${surname}", usernameOrEmail)
                            navController.navigate(HomeDestination())
                        } else {
                            errorValidation()
                        }
                    }
            } else {
                errorValidation()
            }
        }
    }

    fun signInWithGoogle(result: ActivityResult, context: Context, navController: NavController){
        if (result.resultCode != Activity.RESULT_OK) {
            // The user cancelled the login, was it due to an Exception?
            if (result.data?.action == ActivityResultContracts.StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST) {
                val exception = result.data?.getSerializableExtra(ActivityResultContracts.StartIntentSenderForResult.EXTRA_SEND_INTENT_EXCEPTION)
                Log.e("LOG", "Couldn't start One Tap UI: ${exception?.toString()}")
            }
            return
        }
        val oneTapClient = Identity.getSignInClient(context)
        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
        val idToken = credential.googleIdToken
        if (idToken != null) {
            // Got an ID token from Google. Use it to authenticate
            // with your backend.
            Log.i("email", credential.id)
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val name = credential.displayName!!.split(" ")[0]
                        val surname = credential.displayName!!.split(" ")[1]
                        val username = "${name.lowercase()}_${surname.lowercase()}"
                        auth.currentUser?.updateProfile(userProfileChangeRequest {
                            displayName = "$name $surname"
                        })
                        insertUserIfNotExist(name, surname, username, credential.id)
                        navController.navigate(HomeDestination())
                    } else {
                        errorValidation()
                    }
                }
            navController.navigate(HomeDestination())
            Log.d("Token", idToken)
        } else {
            Log.d("Token", "Null Token")
        }
    }

    suspend fun launchSignInWithGoogle(
        context: Context,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
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
            Log.d("LOG", e.message.toString())
        }
    }

    fun isEmail(field: String){
        isEmail = "^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$".toRegex().matches(field)
    }

    fun sendEmailResetPassword(){
        if(emailResetPassword.isNotEmpty()) {
            auth.sendPasswordResetEmail(emailResetPassword)
        }
        viewModelScope.launch {
            snackbarHostState.showSnackbar(if (emailResetPassword.isNotEmpty()) "Password reset link sent" else "Impossible to send the email. Invalid email")
        }
    }

    private fun errorValidation(){
        isError = true
        viewModelScope.launch {
            snackbarHostState.showSnackbar("${if (isEmail) "Email" else "Username"} or password is incorrect")
        }
    }

    private fun insertUserIfNotExist(name: String, surname: String, username: String, email: String){
        viewModelScope.launch {
            if(repository.getUserByUid(auth.currentUser?.uid!!) == null) {
                repository.insertUser(
                    User(
                        auth.currentUser?.uid!!,
                        name,
                        surname,
                        username,
                        email
                    )
                )
            }
        }
    }
}