package it.unibo.gamelibrary

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import it.unibo.gamelibrary.ui.theme.GameLibraryTheme

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            GameLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Signup()
                }
            }
        }
    }

    private fun getCurrentPosition(): List<Double> {
        var position = listOf<Double>()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return position
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                }
                else {
                    position = listOf(location.latitude, location.longitude)
                    Log.d("LOCATION", "${location.latitude}, ${location.longitude}")
                }
            }
        return position
    }

    @Preview
    @Composable
    fun Signup() {
        Scaffold { it ->
            Column(modifier = Modifier
                .padding(it)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    var nameField by rememberSaveable { mutableStateOf("") }
                    var surnameField by rememberSaveable { mutableStateOf("") }
                    var usernameField by rememberSaveable { mutableStateOf("") }
                    /*TODO: check state to remember*/
                    val addressField = remember { mutableStateListOf<Double>() }
                    var emailField by rememberSaveable { mutableStateOf("") }
                    var passwordField by rememberSaveable { mutableStateOf("") }
                    var confirmPasswordField by rememberSaveable { mutableStateOf("") }

                    var isPasswordHidden by rememberSaveable { mutableStateOf(true) }
                    var isPasswordConfirmHidden by rememberSaveable { mutableStateOf(true) }


                    Log.d("LOCATION_PRINT", addressField.toString())

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Text(text = "Signup", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.size(16.dp))
                        TextField(
                            value = nameField,
                            onValueChange = { nameField = it },
                            label = { Text("Name") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "name") }
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        TextField(
                            value = surnameField,
                            onValueChange = { surnameField = it },
                            label = { Text("Surname") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "surname") }
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        TextField(
                            value = usernameField,
                            onValueChange = { usernameField = it },
                            label = { Text("Username") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Person,contentDescription = "username") }
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Row {
                            TextField(
                                value = addressField.toDoubleArray().toString(),
                                onValueChange = { },
                                enabled = false,
                                label = { Text("Address") },
                                placeholder = { Text("") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = "address") },
                                trailingIcon = {
                                    IconButton(onClick = { addressField.addAll(getCurrentPosition()) }) {
                                        Icon(
                                            Icons.Outlined.MyLocation,
                                            contentDescription = "address"
                                        )
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        TextField(
                            value = emailField,
                            onValueChange = { emailField = it },
                            label = { Text("Email") },
                            placeholder = { Text("example@gmail.com") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "email") }
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        TextField(
                            value = passwordField,
                            onValueChange = { passwordField = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = if (isPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "password") },
                            trailingIcon = {
                                IconButton(onClick = {isPasswordHidden = !isPasswordHidden}) {
                                    Icon(
                                        imageVector =  if(isPasswordHidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = if (isPasswordHidden) "hide password" else "show password" )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        TextField(
                            value = confirmPasswordField,
                            onValueChange = { confirmPasswordField = it },
                            label = { Text("Confirm Password") },
                            singleLine = true,
                            visualTransformation = if (isPasswordConfirmHidden) PasswordVisualTransformation() else VisualTransformation.None,
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "confirm password") },
                            trailingIcon = {
                                IconButton(onClick = {isPasswordConfirmHidden = !isPasswordConfirmHidden}) {
                                    Icon(
                                        imageVector =  if(isPasswordConfirmHidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = if (isPasswordConfirmHidden) "hide password" else "show password" )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        OutlinedButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Outlined.Send, contentDescription = "signup")
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Signup")
                        }
                        if (addressField.isNotEmpty()) {
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(text = "Latitudine: ${addressField[0]} e Longitudine: ${addressField[1]}")
                        }
                    }
                }
            }
        }
    }
}

