package it.unibo.gamelibrary.ui.views.Login

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun LoginPage(
    viewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
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
                TextField(
                    value = viewModel.fields["password"]!!,
                    onValueChange = {
                        viewModel.fields["password"] = it
                        viewModel.isError = false
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (viewModel.isPasswordHidden.value) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = "password"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.isPasswordHidden.value = !viewModel.isPasswordHidden.value }) {
                            Icon(
                                imageVector = if (viewModel.isPasswordHidden.value) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (viewModel.isPasswordHidden.value) "hide password" else "show password"
                            )
                        }
                    },
                    isError = viewModel.isError,
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(onClick = {
                    viewModel.login(navController)
                }) {
                    Icon(Icons.Outlined.Login, contentDescription = "login")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "Login")
                }
                Spacer(modifier = Modifier.size(32.dp))
                Row {
                    Text( = "Password forgot?")
                    ClickableText(text = "Password forgot?")
                }
            }
        }

    }
}