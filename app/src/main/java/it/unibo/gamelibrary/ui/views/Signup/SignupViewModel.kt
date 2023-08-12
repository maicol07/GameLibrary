package it.unibo.gamelibrary.ui.views.Signup

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    var fieldsErrors = mutableStateMapOf(
        Pair("name", false),
        Pair("surname", false),
        Pair("username", false),
        //Pair("address", false),
        Pair("email", false),
        Pair("password", false),
        Pair("confirmPassword", false)
    )
    val fields = mutableStateMapOf(
        Pair("name", ""),
        Pair("surname", ""),
        Pair("username", ""),
        //Pair("address", ""),
        Pair("email", ""),
        Pair("password", ""),
        Pair("confirmPassword", "")
    )
    private var isSignupButtonPressed by mutableStateOf(false)

    var isPasswordHidden by mutableStateOf(true)
    var isPasswordConfirmHidden by mutableStateOf(true)

    fun signUp(navController: NavController) {
        if (!isSignupButtonPressed) {
            Log.i("Signup", "Signup pressed")
            isSignupButtonPressed = true
            if (checkErrors()) {
                Log.i("Signup firebase", "${fields["email"]!!}, ${fields["password"]!!}" )
                auth.createUserWithEmailAndPassword(fields["email"]!!, fields["password"]!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("AuthEmail", "createUserWithEmail:success")
                            val user = User(
                                auth.currentUser?.uid!!,
                                fields["name"]!!,
                                fields["surname"]!!,
                                fields["username"]!!,
                                fields["email"]!!
                            )
                            insertUser(user)
                            auth.currentUser?.updateProfile(userProfileChangeRequest {
                                displayName = "${user.name} ${user.surname}"
                            })
                            navController.navigate(HomeDestination())
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignupFirebase", "createUserWithEmail:failure", task.exception)
                            viewModelScope.launch {
                                snackbarHostState.showSnackbar("User already registered")
                            }
                        }
                    }
            }
            isSignupButtonPressed = false
        }
    }

    private fun insertUser(user: User) = viewModelScope.launch {
        repository.insertUser(user)
    }

    private fun validate(field: String, validate: (field: String) -> Boolean): Boolean {
        return validate(field)
    }

    private fun checkErrors(): Boolean {
        for ((key, value) in fields.entries) {
            fieldsErrors[key] = validate(value) { f ->
                when (key) {
                    "email" -> !"^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$".toRegex()
                        .matches(f)
                    "password" -> f.isEmpty() || f.length < 6
                    "confirmPassword" -> f.isEmpty() || f != fields["password"]
                    else -> f.isEmpty()
                }
            }
        }

        for (value in fieldsErrors.values) {
            if (value){
                return false
            }
        }
        return true
    }
}