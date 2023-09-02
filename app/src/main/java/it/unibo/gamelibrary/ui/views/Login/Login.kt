package it.unibo.gamelibrary.ui.views.Login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.common.components.PasswordTextfield
import it.unibo.gamelibrary.ui.destinations.SignupPageDestination
import kotlinx.coroutines.launch

@Destination
@Composable
fun LoginPage(
    viewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            viewModel.signInWithGoogle(result, context, navController)
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Login", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = viewModel.fields["usernameOrEmail"]!!,
                    onValueChange = {
                        viewModel.fields["usernameOrEmail"] = it
                        viewModel.isEmail(it)
                        viewModel.isError = false
                    },
                    label = { Text("Username or Email") },
                    singleLine = true,
                    leadingIcon = {
                        Crossfade(targetState = viewModel.isEmail, label = "Change icon") {
                            Icon(
                                if (it) Icons.Outlined.Email else Icons.Outlined.Person,
                                contentDescription = "usernameOrEmail"
                            )
                        }
                    },
                    isError = viewModel.isError,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )
                Spacer(modifier = Modifier.size(16.dp))
                PasswordTextfield(
                    value = viewModel.fields["password"]!!,
                    onChange = {
                        viewModel.fields["password"] = it
                        viewModel.isError = false
                    },
                    label = "Password",
                    isHidden = viewModel.isPasswordHidden.value,
                    onPasswordVisible = {
                        viewModel.isPasswordHidden.value = !viewModel.isPasswordHidden.value
                    },
                    isError = viewModel.isError
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(onClick = {
                    viewModel.login(navController)
                }) {
                    Icon(Icons.Outlined.Login, contentDescription = "login")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "Login")
                }
                TextButton(
                    onClick = { viewModel.openResetPasswordDialog = true }
                ) {
                    Text("Password forgot?")
                }
                Text("Or login with:")
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            viewModel.launchSignInWithGoogle(
                                context = context,
                                launcher = launcher
                            )
                        }
                    }
                ) {
                    Icon(
                        painterResource(id = R.drawable.google),
                        contentDescription = "signin"
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "Google")
                }
                TextButton(
                    onClick = { navController.navigate(SignupPageDestination()) }
                ) {
                    Text("Don't have an account? Signup")
                }
            }
            if (viewModel.openResetPasswordDialog) {
                ResetPasswordDialog(viewModel)
            }
        }
    }
}

@Composable
fun ResetPasswordDialog(viewModel: LoginViewModel = hiltViewModel()) {
    val focusManager = LocalFocusManager.current
    CustomDialog(
        onDismissRequest = { viewModel.openResetPasswordDialog = false },
        buttons = {
            TextButton(
                onClick = {
                    viewModel.sendEmailResetPassword()
                    viewModel.openResetPasswordDialog = false
                }
            ) {
                Icon(Icons.Outlined.Send, contentDescription = "send")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = "Send")
            }
        },
        icon = { Icon(Icons.Outlined.LockReset, "reset password") },
        title = {
            Text(
                text = "Reset password",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Insert your email to receive a mail to reset the password")
            Spacer(modifier = Modifier.size(16.dp))
            TextField(
                value = viewModel.emailResetPassword,
                onValueChange = {
                    viewModel.emailResetPassword = it
                },
                label = { Text("Email") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = "usernameOrEmail"
                    )
                },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )
        }
    }
}