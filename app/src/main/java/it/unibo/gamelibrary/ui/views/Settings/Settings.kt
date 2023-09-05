package it.unibo.gamelibrary.ui.views.Settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreIntSettingState
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.ramcosta.composedestinations.annotation.Destination
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.common.components.PasswordTextfield
import it.unibo.gamelibrary.utils.TopAppBarState
import it.unibo.gamelibrary.utils.snackBarHostState
import kotlinx.coroutines.launch

@Composable
@Destination
fun SettingsPage(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    val darkState = rememberPreferenceDataStoreIntSettingState(key = "dark theme", defaultValue = 0)
    val context = LocalContext.current
    viewModel.getAddress()

    TopAppBarState.title = "Settings"
    TopAppBarState.actions = {}

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            SettingsList(
                state = darkState,
                title = { Text(text = "UI theme") },
                items = listOf("System", "Dark", "Light"),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Brightness4,
                        contentDescription = "Dark theme"
                    )
                },
                subtitle = { Text(text = "Choose your app theme") },
                onItemSelected = { index, _ -> darkState.value = index },
                modifier = Modifier.height(72.dp)
            )
        }

        items(viewModel.settingsList) { setting ->
            if (setting.type === SettingsTypeEnum.Switch) {
                val state = setting.switchState!!()
                SettingsSwitch(
                    icon = setting.icon,
                    title = { Text(text = setting.title) },
                    subtitle = { Text(text = setting.subtitle) },
                    state = state,
                    modifier = Modifier.height(if (setting.subtitle.isEmpty()) 72.dp else 88.dp),
                    enabled = setting.enabled(viewModel, context),
                    onCheckedChange = { setting.onCheckedChange!!(viewModel, context, state) }
                )
            } else {
                SettingsMenuLink(
                    icon = setting.icon,
                    title = {
                        Text(
                            text = setting.title,
                            color = if (setting.title == "Logout") MaterialTheme.colorScheme.error else Color.Unspecified
                        )
                    },
                    subtitle = { if (setting.subtitle.isNotEmpty()) Text(text = setting.subtitle) },
                    onClick = { setting.onClick?.invoke(viewModel) },
                    modifier = Modifier.height(if (setting.subtitle.isEmpty()) 72.dp else 88.dp),
                    action = if (setting.action != null) { _ -> setting.action?.invoke(viewModel) } else null,
                    enabled = setting.enabled(viewModel, context)
                )
            }
        }
    }
    if (viewModel.openEmailDialog) {
        ChangeEmailDialog()
    } else if (viewModel.openPasswordDialog) {
        ChangePasswordDialog()
    } else if (viewModel.openLocationDialog) {
        CheckPermission()
    } else if (viewModel.openLogoutDialog) {
        LogoutDialog(navController = navController)
    }
}

@Composable
private fun ChangeEmailDialog(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            viewModel.changeEmail(result, context)
        }
    CustomDialog(
        onDismissRequest = { viewModel.openEmailDialog = false },
        modifier = Modifier.padding(16.dp),
        buttons = {
            TextButton(
                onClick = { viewModel.openEmailDialog = false }
            ) {
                Text(text = "Cancel")
            }
            TextButton(
                onClick = {
                    scope.launch {
                        viewModel.launchChange(
                            context = context,
                            launcher = launcher
                        )
                    }
                }
            ) {
                Text(text = "Ok")
            }
        },
        icon = {
            Icon(
                painterResource(id = R.drawable.email_edit),
                contentDescription = "Edit Email"
            )
        },
        title = { Text("Edit Email") }
    ) {
        Column {
            Text("Insert your new email")
            Spacer(modifier = Modifier.size(16.dp))
            TextField(
                value = viewModel.emailValue,
                onValueChange = {
                    viewModel.emailValue = it
                },
                label = { Text("Email") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = "email"
                    )
                },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )
            if (!viewModel.isSignInWithGoogle) {
                Spacer(modifier = Modifier.padding(16.dp))
                Text("Insert your password")
                Spacer(modifier = Modifier.padding(16.dp))
                PasswordTextfield(
                    Modifier,
                    viewModel.passwordValue,
                    { viewModel.passwordValue = it },
                    "Password",
                    viewModel.isHidden,
                    { viewModel.isHidden = !viewModel.isHidden }
                )
            }
        }
    }
}

@Composable
private fun ChangePasswordDialog(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            viewModel.changePassword(result, context)
        }
    CustomDialog(
        onDismissRequest = { viewModel.openPasswordDialog = false },
        modifier = Modifier
            .padding(16.dp)
            .requiredWidth(320.dp),
        buttons = {
            TextButton(
                onClick = { viewModel.openPasswordDialog = false }
            ) {
                Text(text = "Cancel")
            }
            TextButton(
                onClick = {
                    scope.launch {
                        viewModel.launchChange(
                            context = context,
                            launcher = launcher
                        )
                    }
                }
            ) {
                Text(text = "Ok")
            }
        },
        icon = {
            Icon(
                Icons.Default.LockReset,
                contentDescription = "Edit password"
            )
        },
        title = { Text("Edit password") }
    ) {
        Column {
            if (!viewModel.isSignInWithGoogle) {
                PasswordTextfield(
                    Modifier.fillMaxWidth(),
                    viewModel.passwordFields["old"]!!,
                    { viewModel.passwordFields["old"] = it },
                    "Old password",
                    viewModel.passwordsHidden["old"]!!,
                    { viewModel.passwordsHidden["old"] = !viewModel.passwordsHidden["old"]!! }
                )
                Spacer(modifier = Modifier.padding(16.dp))
            }
            PasswordTextfield(
                Modifier.fillMaxWidth(),
                viewModel.passwordFields["new"]!!,
                { viewModel.passwordFields["new"] = it },
                "New password",
                viewModel.passwordsHidden["new"]!!,
                { viewModel.passwordsHidden["new"] = !viewModel.passwordsHidden["new"]!! }
            )
            Spacer(modifier = Modifier.padding(16.dp))
            PasswordTextfield(
                Modifier.fillMaxWidth(),
                viewModel.passwordFields["confirm"]!!,
                { viewModel.passwordFields["confirm"] = it },
                "Confirm password",
                viewModel.passwordsHidden["confirm"]!!,
                { viewModel.passwordsHidden["confirm"] = !viewModel.passwordsHidden["confirm"]!! }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CheckPermission(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    viewModel.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
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
        val buttonText =
            if (!allPermissionsRevoked) "Allow precise location" else "Request permissions"
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                viewModel.openLocationDialog = false
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
                        viewModel.viewModelScope.launch {
                            snackBarHostState.showSnackbar("Re-press the button to change your address")
                        }
                        viewModel.openLocationDialog = false
                    }
                ) {
                    Text(buttonText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.openLocationDialog = false
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
private fun LogoutDialog(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    CustomDialog(
        onDismissRequest = { viewModel.openLogoutDialog = false },
        modifier = Modifier.padding(16.dp),
        buttons = {
            TextButton(
                onClick = { viewModel.openLogoutDialog = false }
            ) {
                Text(text = "Cancel")
            }
            TextButton(
                onClick = {
                    viewModel.logout(navController)
                }
            ) {
                Text(text = "Logout")
            }
        },
        icon = {
            Icon(
                Icons.Default.Logout,
                contentDescription = "Logout"
            )
        },
        title = { Text("Logout") }
    ) {
        Column {
            Text("Are you sure you want to log out?")
        }
    }
}
