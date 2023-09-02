package it.unibo.gamelibrary.ui.views.Signup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.common.components.PasswordTextfield
import it.unibo.gamelibrary.ui.destinations.LoginPageDestination
import it.unibo.gamelibrary.ui.views.Login.LoginViewModel
import kotlinx.coroutines.launch

@Destination
@Composable
fun SignupPage(
    viewModel: SignupViewModel = hiltViewModel(),
    navController: NavController
) {
    viewModel.getListCompanies()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            loginViewModel.signInWithGoogle(result, context, navController)
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Switch(
                checked = viewModel.isPublisher,
                onCheckedChange = { viewModel.isPublisher = !viewModel.isPublisher }
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text("Are you a publisher?")
        }
        Spacer(modifier = Modifier.size(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Signup", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.size(16.dp))
                if (!viewModel.isPublisher) {
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
                        supportingText = { if (viewModel.fieldsErrors["name"]!!) Text(text = "Name field is required") }
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
                        supportingText = { if (viewModel.fieldsErrors["surname"]!!) Text(text = "Surname field is required") }
                    )
                } else {
                    PublisherExposedDropdownMenu()
                }
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
                    supportingText = { if (viewModel.fieldsErrors["username"]!!) Text(text = "Username field is required") }
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
                    supportingText = { if (viewModel.fieldsErrors["email"]!!) Text(text = "Email field is not a mail") }
                )
                Spacer(modifier = Modifier.size(16.dp))
                PasswordTextfield(
                    value = viewModel.fields["password"]!!,
                    onChange = { viewModel.fields["password"] = it },
                    label = "Password",
                    isHidden = viewModel.isPasswordHidden,
                    onPasswordVisible = {
                        viewModel.isPasswordHidden = !viewModel.isPasswordHidden
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = "password"
                        )
                    },
                    isError = viewModel.fieldsErrors["password"]!!,
                    supportingText = { if (viewModel.fieldsErrors["password"]!!) Text(text = "Password field has less than 6 characters") }
                )
                Spacer(modifier = Modifier.size(16.dp))
                PasswordTextfield(
                    value = viewModel.fields["confirmPassword"]!!,
                    onChange = { viewModel.fields["confirmPassword"] = it },
                    label = "Confirm password",
                    isHidden = viewModel.isPasswordConfirmHidden,
                    onPasswordVisible = {
                        viewModel.isPasswordConfirmHidden = !viewModel.isPasswordConfirmHidden
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(id = R.drawable.lock_check_outline),
                            contentDescription = "confirm password"
                        )
                    },
                    isError = viewModel.fieldsErrors["confirmPassword"]!!,
                    supportingText = { if (viewModel.fieldsErrors["confirmPassword"]!!) Text(text = "Passwords are different") }
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(onClick = {
                    viewModel.signUp(navController)
                }) {
                    Icon(Icons.Outlined.Send, contentDescription = "signup")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "Signup")
                }
                if (!viewModel.isPublisher) {
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
                }
                TextButton(
                    onClick = { navController.navigate(LoginPageDestination()) }
                ) {
                    Text("Do you already have an account? Sign in")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublisherExposedDropdownMenu(viewModel: SignupViewModel = hiltViewModel()) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            // The `menuAnchor` modifier must be passed to the text field for correctness.
            modifier = Modifier.menuAnchor(),
            value = viewModel.publisherField,
            onValueChange = {
                viewModel.publisherField = it
                viewModel.getListCompanies(it.text)
            },
            label = { Text("Publisher") },
            leadingIcon = { Icon(Icons.Outlined.Business, "business") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            isError = viewModel.publisherError,
            supportingText = { if (viewModel.publisherError) Text("The publisher name doesn't exist") },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            viewModel.publisherOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.name) },
                    onClick = {
                        viewModel.publisherField = TextFieldValue(
                            text = selectionOption.name,
                            selection = TextRange(selectionOption.name.length)
                        )
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

//  TODO: [POSSIBILE BUG] Verificare se premendo signup più volte si creano più utenti (o comunque se viene chiamato più volte il metodo)
//      [LOW PRIORITY] Vedere se c'è qualche design migliore per la pagina di signup