package it.unibo.gamelibrary.ui.views.Signup

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
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
import it.unibo.gamelibrary.ui.destinations.HomeDestination
import it.unibo.gamelibrary.utils.IGDBClient
import it.unibo.gamelibrary.utils.SafeRequest
import it.unibo.gamelibrary.utils.snackBarHostState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.pixnews.igdbclient.apicalypse.SortOrder
import ru.pixnews.igdbclient.getCompanies
import ru.pixnews.igdbclient.model.Company
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
        Pair("email", false),
        Pair("password", false),
        Pair("confirmPassword", false)
    )
    val fields = mutableStateMapOf(
        Pair("name", ""),
        Pair("surname", ""),
        Pair("username", ""),
        Pair("email", ""),
        Pair("password", ""),
        Pair("confirmPassword", "")
    )
    private var job: Job? = null
    private var publisherSlug by mutableStateOf<String?>(null)
    var publisherOptions by mutableStateOf<List<Company>>(emptyList())
    var isPublisher by mutableStateOf(false)
    var publisherField by mutableStateOf(TextFieldValue(""))
    var publisherError by mutableStateOf(false)

    private var isSignupButtonPressed by mutableStateOf(false)

    var isPasswordHidden by mutableStateOf(true)
    var isPasswordConfirmHidden by mutableStateOf(true)

    fun signUp(navController: NavController) {
        if (!isSignupButtonPressed) {
            Log.i("Signup", "Signup pressed")
            isSignupButtonPressed = true
            if (checkErrors()) {
                Log.i("Signup firebase", "${fields["email"]!!}, ${fields["password"]!!}")
                auth.createUserWithEmailAndPassword(fields["email"]!!, fields["password"]!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("AuthEmail", "createUserWithEmail:success")
                            var user: User?
                            if (!isPublisher) {
                                user = User(
                                    auth.currentUser?.uid!!,
                                    fields["name"]!!,
                                    fields["surname"]!!,
                                    fields["username"]!!,
                                    fields["email"]!!,
                                    isPublisher = isPublisher
                                )
                                viewModelScope.launch {
                                    repository.insertUser(user!!)
                                }
                                auth.currentUser?.updateProfile(userProfileChangeRequest {
                                    displayName = "${user?.name} ${user?.surname}"
                                    photoUri = null
                                })
                                navController.navigate(HomeDestination())
                            } else {
                                getSlugByCompany(publisherField.text).invokeOnCompletion {
                                    user = User(
                                        uid = auth.currentUser?.uid!!,
                                        username = fields["username"]!!,
                                        email = fields["email"]!!,
                                        isPublisher = isPublisher,
                                        publisherName = publisherSlug
                                    )
                                    viewModelScope.launch {
                                        repository.insertUser(user!!)
                                    }
                                    auth.currentUser?.updateProfile(userProfileChangeRequest {
                                        displayName = publisherField.text
                                        photoUri = Uri.parse("https://prova.com")
                                    })
                                    navController.navigate(HomeDestination())
                                }
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(
                                "SignupFirebase",
                                "createUserWithEmail:failure",
                                task.exception
                            )
                            viewModelScope.launch {
                                snackBarHostState.showSnackbar("User already registered")
                            }
                        }
                    }
            }
            isSignupButtonPressed = false
        }
    }

    fun getListCompanies(name: String = "") {
        job?.cancel()
        job = viewModelScope.launch {
            val result = SafeRequest {
                IGDBClient.getCompanies {
                    fields("name")
                    where("name ~ *\"${name}\"*")
                    limit(50)
                    sort("name", SortOrder.ASC)
                }
            }
            publisherOptions = result?.companies ?: emptyList()
        }
    }

    private fun getSlugByCompany(name: String): Job = viewModelScope.launch {
        val result = SafeRequest {
            IGDBClient.getCompanies {
                fields("slug")
                where("name = \"${name}\"")
                sort("slug", SortOrder.ASC)
                limit(1)
            }
        }
        publisherSlug = result?.companies?.firstOrNull()?.slug
    }

    private fun validate(field: String, validate: (field: String) -> Boolean): Boolean {
        return validate(field)
    }

    private fun checkErrors(): Boolean {
        for ((key, value) in fields.entries) {
            fieldsErrors[key] = validate(value) { f ->
                when (key) {
                    "username" -> f.isEmpty()
                    "email" -> !"^((?!\\.)[\\w-_.]*[^.])(@\\w+)(\\.\\w+(\\.\\w+)?[^.\\W])\$".toRegex()
                        .matches(f)

                    "password" -> f.isEmpty() || f.length < 6
                    "confirmPassword" -> f.isEmpty() || f != fields["password"]
                    else -> if (isPublisher) false else f.isEmpty()
                }
            }
        }
        if (isPublisher) {
            publisherError = publisherField.text.isEmpty() || !publisherOptions.map { it.name }
                .contains(publisherField.text)
        }
        for (value in fieldsErrors.values) {
            if (value) {
                return false
            }
        }
        return if (isPublisher) !publisherError else true
    }
}