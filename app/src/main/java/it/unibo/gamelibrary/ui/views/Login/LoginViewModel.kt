package it.unibo.gamelibrary.ui.views.Login

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
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.launch
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

    fun isEmail(field: String){
        isEmail = "^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$".toRegex().matches(field)
    }

    private fun errorValidation(){
        isError = true
        viewModelScope.launch {
            snackbarHostState.showSnackbar("${if (isEmail) "Email" else "Username"} or password is incorrect")
        }
    }
}