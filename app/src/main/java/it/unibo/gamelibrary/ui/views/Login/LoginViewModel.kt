package it.unibo.gamelibrary.ui.views.Login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
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
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.Secrets
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.ui.destinations.HomeDestination
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.flow.first
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

    fun login(navController: NavController) {
        viewModelScope.launch {
            var usernameOrEmail = fields["usernameOrEmail"]!!
            if (!isEmail) {
                val user = repository.getUserByUsername(usernameOrEmail).first()
                usernameOrEmail = user?.email ?: ""
            }
            Log.i("Email", usernameOrEmail)
            if (usernameOrEmail.isNotEmpty() && fields["password"]!!.isNotEmpty()) {
                auth.signInWithEmailAndPassword(usernameOrEmail, fields["password"]!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val isPublisher = auth.currentUser?.photoUrl != null
                            if (isPublisher) {
                                insertUserIfNotExist(
                                    username = auth.currentUser?.displayName!!,
                                    email = usernameOrEmail,
                                    isPublisher = true,
                                    publisherName = auth.currentUser!!.displayName
                                ).invokeOnCompletion { navController.navigate(HomeDestination()) }
                            } else {
                                val displayName = auth.currentUser?.displayName?.split(" ")
                                val name = displayName?.get(0) ?: ""
                                val surname =
                                    if (displayName?.getOrNull(1) == null) "" else displayName[1]
                                insertUserIfNotExist(
                                    name,
                                    if (surname == "") null else surname,
                                    if (surname == "") name else "${name}_${surname}",
                                    usernameOrEmail
                                ).invokeOnCompletion { navController.navigate(HomeDestination()) }
                            }
                        } else {
                            errorValidation()
                        }
                    }
            } else {
                errorValidation()
            }
        }
    }

    fun signInWithGoogle(result: ActivityResult, context: Context, navController: NavController) {
        if (result.resultCode != Activity.RESULT_OK) {
            viewModelScope.launch {
                snackbarHostState.showSnackbar("Google login cancelled")
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
                        var isUserPublisher = false
                        viewModelScope.launch {
                            isUserPublisher =
                                repository.getUserByUid(auth.currentUser?.uid!!)
                                    .first()?.isPublisher == true
                        }.invokeOnCompletion {
                            if (isUserPublisher) {
                                insertUserIfNotExist(
                                    username = credential.displayName!!,
                                    email = credential.id,
                                    isPublisher = true,
                                    publisherName = credential.displayName!!
                                )
                            } else {
                                val name = credential.displayName!!.split(" ")[0]
                                val surname = credential.displayName!!.split(" ")[1]
                                insertUserIfNotExist(
                                    name,
                                    surname,
                                    "${name.lowercase()}_${surname.lowercase()}",
                                    credential.id
                                )
                            }
                            navController.navigate(HomeDestination())
                        }
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

    fun isEmail(field: String) {
        isEmail =
            "^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$".toRegex().matches(field)
    }

    fun sendEmailResetPassword() {
        if (emailResetPassword.isNotEmpty()) {
            auth.sendPasswordResetEmail(emailResetPassword)
        }
        viewModelScope.launch {
            snackbarHostState.showSnackbar(if (emailResetPassword.isNotEmpty()) "Password reset link sent" else "Impossible to send the email. Invalid email")
        }
    }

    private fun errorValidation() {
        isError = true
        viewModelScope.launch {
            snackbarHostState.showSnackbar("${if (isEmail) "Email" else "Username"} or password is incorrect")
        }
    }

    private fun insertUserIfNotExist(
        name: String? = null,
        surname: String? = null,
        username: String,
        email: String,
        isPublisher: Boolean = false,
        publisherName: String? = null
    ) = viewModelScope.launch {
        if (repository.getUserByUid(auth.currentUser?.uid!!).first() == null) {
            repository.insertUser(
                if (isPublisher)
                    User(
                        uid = auth.currentUser?.uid!!,
                        username = username,
                        email = email,
                        isPublisher = true,
                        publisherName = publisherName
                    )
                else
                    User(
                        uid = auth.currentUser?.uid!!,
                        name = name,
                        surname = surname,
                        username = username,
                        email = email,
                        isPublisher = false
                    )
            )
        }
    }
}
