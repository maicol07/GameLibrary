package it.unibo.gamelibrary.ui.views.Signup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import it.unibo.gamelibrary.MainActivity
import it.unibo.gamelibrary.data.model.User
import it.unibo.gamelibrary.data.repository.UserRepository
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import it.unibo.gamelibrary.utils.snackbarHostState
import kotlinx.coroutines.launch
import java.util.Locale
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

    lateinit var fusedLocationClient: FusedLocationProviderClient
    private val auth: FirebaseAuth = Firebase.auth
    val addressField = mutableStateListOf<Double>()
    var fieldsErrors = mutableStateMapOf(
        Pair("name", false),
        Pair("surname", false),
        Pair("username", false),
        Pair("address", false),
        Pair("email", false),
        Pair("password", false),
        Pair("confirmPassword", false)
    )
    val fields = mutableStateMapOf(
        Pair("name", ""),
        Pair("surname", ""),
        Pair("username", ""),
        Pair("address", ""),
        Pair("email", ""),
        Pair("password", ""),
        Pair("confirmPassword", "")
    )
    var address: Address? = null

    var isLocalizationStarted = mutableStateOf(false)
    var isLocalizationFailed = mutableStateOf(false)
    var isSignupButtonPressed = mutableStateOf(false)

    var isPasswordHidden = mutableStateOf(true)
    var isPasswordConfirmHidden = mutableStateOf(true)

    var isPermissionGranted = mutableStateOf(false)
    var isDialogOpen = mutableStateOf(false)

    fun getCurrentPosition(@ActivityContext activityContext: Context) {
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
    }

    fun getLocation(
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
    }

    fun signUp(navController: NavController) {
        if (!isSignupButtonPressed.value) {
            Log.i("Signup", "Signup pressed")
            isSignupButtonPressed.value = true
            if (checkErrors()) {
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
                                "${address?.latitude} ${address?.longitude}"
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
            isSignupButtonPressed.value = false
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
                    "email" -> !"^[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*$".toRegex()
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