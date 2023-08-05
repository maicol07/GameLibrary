package it.unibo.gamelibrary.ui.views.Signup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.views.Login.LoginViewModel
import it.unibo.gamelibrary.ui.views.destinations.LoginPageDestination
import kotlinx.coroutines.launch

@Destination
@Composable
fun SignupPage(
    viewModel: SignupViewModel = hiltViewModel(),
    navController: NavController
) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        loginViewModel.signInWithGoogle(result, context, navController)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Signup", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = viewModel.fields["name"]!!,
                    onValueChange = { viewModel.fields["name"] = it },
                    label = { Text("Name") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "name"
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = viewModel.fieldsErrors["name"]!!,
                    supportingText = { if (viewModel.fieldsErrors["name"]!!) Text(text = "Name field is required") else Unit }
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = viewModel.fields["surname"]!!,
                    onValueChange = { viewModel.fields["surname"] = it },
                    label = { Text("Surname") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "surname"
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    isError = viewModel.fieldsErrors["surname"]!!,
                    supportingText = { if (viewModel.fieldsErrors["surname"]!!) Text(text = "Surname field is required") else Unit }
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = viewModel.fields["username"]!!,
                    onValueChange = { viewModel.fields["username"] = it },
                    label = { Text("Username") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "username"
                        )
                    },
                    isError = viewModel.fieldsErrors["username"]!!,
                    supportingText = { if (viewModel.fieldsErrors["username"]!!) Text(text = "Username field is required") else Unit }
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = viewModel.fields["email"]!!,
                    onValueChange = { viewModel.fields["email"] = it },
                    label = { Text("Email") },
                    placeholder = { Text("example@gmail.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = "email"
                        )
                    },
                    isError = viewModel.fieldsErrors["email"]!!,
                    supportingText = { if (viewModel.fieldsErrors["email"]!!) Text(text = "Email field is not a mail") else Unit }
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = viewModel.fields["password"]!!,
                    onValueChange = { viewModel.fields["password"] = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (viewModel.isPasswordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = "password"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.isPasswordHidden = !viewModel.isPasswordHidden }) {
                            Icon(
                                imageVector = if (viewModel.isPasswordHidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (viewModel.isPasswordHidden) "hide password" else "show password"
                            )
                        }
                    },
                    isError = viewModel.fieldsErrors["password"]!!,
                    supportingText = { if (viewModel.fieldsErrors["password"]!!) Text(text = "Password field has less than 6 characters") else Unit }
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = viewModel.fields["confirmPassword"]!!,
                    onValueChange = { viewModel.fields["confirmPassword"] = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (viewModel.isPasswordConfirmHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = {
                        Icon(
                            painterResource(id = R.drawable.lock_check_outline),
                            contentDescription = "confirm password"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.isPasswordConfirmHidden = !viewModel.isPasswordConfirmHidden
                        }) {
                            Icon(
                                imageVector = if (viewModel.isPasswordConfirmHidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (viewModel.isPasswordConfirmHidden) "hide password" else "show password"
                            )
                        }
                    },
                    isError = viewModel.fieldsErrors["confirmPassword"]!!,
                    supportingText = { if (viewModel.fieldsErrors["confirmPassword"]!!) Text(text = "Passwords are different") else Unit }
                )
                Spacer(modifier = Modifier.size(16.dp))
                //Row(horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    viewModel.signUp(navController)
                }) {
                    Icon(Icons.Outlined.Send, contentDescription = "signup")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "Signup")
                }
                Spacer(modifier = Modifier.size(16.dp))
                Text("Or signup with:")
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            loginViewModel.launchSignInWithGoogle(
                                context = context,
                                launcher = launcher
                            )
                        }
                    }
                ) {
                    Icon(
                        painterResource(id = R.drawable.google),
                        contentDescription = "sign in"
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "Google")
                }
                //Spacer(modifier = Modifier.size(4.dp))
                TextButton(
                    onClick = { navController.navigate(LoginPageDestination()) }
                ) {
                    Text("Do you already have an account? Sign in")
                }
            }
        }
    }
}

/*@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CheckPermission(viewModel: SignupViewModel = hiltViewModel()){
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    if (locationPermissionsState.allPermissionsGranted) {
        viewModel.getCurrentPosition(LocalContext.current)
    } else {
        val allPermissionsRevoked =
            locationPermissionsState.permissions.size ==
                    locationPermissionsState.revokedPermissions.size

        val textToShow = if (!allPermissionsRevoked) {
            // If not all the permissions are revoked, it's because the user accepted the COARSE
            // location permission, but not the FINE one.
            "You have granted permissions only for the approximate location but not the exact one. Please grant permissions for the exact location as well"
        } else if (locationPermissionsState.shouldShowRationale) {
            // Both location permissions have been denied
            "Getting your exact location is important for this app. " +
                    "Please grant us fine location."
        } else {
            // First time the user sees this feature or the user doesn't want to be asked again
            "This feature requires location permission"
        }
        val buttonText = if (!allPermissionsRevoked) "Allow precise location" else "Request permissions"
        AlertDialog(
            onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onDismissRequest.
                viewModel.isDialogOpen.value = false
            },
            title = {
                Text(text = "Permissions required")
            },
            text = {
                Text(text = textToShow)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        locationPermissionsState.launchMultiplePermissionRequest()
                        viewModel.isDialogOpen.value = false
                    }
                ) {
                    Text(buttonText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.isDialogOpen.value = false
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}*/

//  TODO: [POSSIBILE BUG] Verificare se premendo signup più volte si creano più utenti (o comunque se viene chiamato più volte il metodo)
//      [LOW PRIORITY] Vedere se c'è qualche design migliore per la pagina di signup