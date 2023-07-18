package it.unibo.gamelibrary.ui.views.Signup

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.launch
import javax.inject.Inject

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    //lateinit var fusedLocationClient: FusedLocationProviderClient
    private val auth: FirebaseAuth = Firebase.auth
    //val addressField = mutableStateListOf<Double>()
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
    //var address: Address? = null

    //var isLocalizationStarted = mutableStateOf(false)
    //var isLocalizationFailed = mutableStateOf(false)
    var isSignupButtonPressed by mutableStateOf(false)

    var isPasswordHidden by mutableStateOf(true)
    var isPasswordConfirmHidden by mutableStateOf(true)

    //var isPermissionGranted = mutableStateOf(false)
    //var isDialogOpen = mutableStateOf(false)

    /*fun getCurrentPosition(@ActivityContext activityContext: Context) {
        val activity = activityContext.findActivity() as MainActivity
        isLocalizationStarted.value = true
        Toast.makeText(activityContext, "Start Location", Toast.LENGTH_SHORT).show()
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
                addressField.clear()
                addressField.addAll(listOf(location.latitude, location.longitude))
                Log.d("LOCATION", "${location.latitude}, ${location.longitude}")
            }
            isLocalizationStarted.value = false
            isLocalizationFailed.value = false
        }
    }*/

    /*fun getLocation(
        @ApplicationContext context: Context,
        onGeocode: (addressName: Address) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            Geocoder(
                context,
                Locale.getDefault()
            ).getFromLocation(
                addressField.toDoubleArray()[0],
                addressField.toDoubleArray()[1],
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        Log.d("ADDRESS", addresses[0].toString())
                        addressField.clear()
                        onGeocode.invoke(addresses[0])
                    }

                    override fun onError(errorMessage: String?) {
                        super.onError(errorMessage)
                        isLocalizationFailed.value = true
                    }
                }
            )
        } else {
            @Suppress("DEPRECATION")
            val address = Geocoder(
                context,
                Locale.getDefault()
            ).getFromLocation(
                addressField.toDoubleArray()[0],
                addressField.toDoubleArray()[1],
                1
            )?.get(0)
            if (address != null) {
                onGeocode.invoke(address)
            }
        }
    }*/

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
                                //"${address?.latitude} ${address?.longitude}"
                            )
                            insertUser(user)
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