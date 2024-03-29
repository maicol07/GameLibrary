package it.unibo.gamelibrary.ui.views.settings

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.alorma.compose.settings.storage.datastore.GenericPreferenceDataStoreSettingValueState
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import it.unibo.gamelibrary.R
import it.unibo.gamelibrary.ui.common.components.checkInternetConnection

enum class SettingsEnum(
    var type: SettingsTypeEnum,
    var icon: @Composable () -> Unit,
    var title: String,
    var subtitle: String,
    var onClick: ((viewModel: SettingsViewModel) -> Unit)? = null,
    var action: @Composable ((viewModel: SettingsViewModel) -> Unit)? = null,
    var enabled: (viewModel: SettingsViewModel, context: Context) -> Boolean = { _, _ -> true },
    val switchState: (@Composable () -> GenericPreferenceDataStoreSettingValueState<Boolean>)? = null,
    var onCheckedChange: ((viewModel: SettingsViewModel, context: Context, state: GenericPreferenceDataStoreSettingValueState<Boolean>) -> Unit)? = null
) {
    EditEmail(SettingsTypeEnum.MenuLink, {
        Icon(
            painterResource(id = R.drawable.email_edit),
            contentDescription = "Change Email"
        )
    }, "Edit Email", "", { viewModel ->
        viewModel.isSignInWithGoogle = viewModel.auth.currentUser?.providerData?.any { profile ->
            profile.providerId == com.google.firebase.auth.GoogleAuthProvider.PROVIDER_ID
        } ?: false
        viewModel.openEmailDialog = true
    }, enabled = { _, context -> checkInternetConnection(context) }),
    EditPassword(SettingsTypeEnum.MenuLink, {
        Icon(
            Icons.Default.LockReset,
            contentDescription = "Change Password"
        )
    }, "Edit Password", "", { viewModel ->
        viewModel.isSignInWithGoogle = viewModel.auth.currentUser?.providerData?.any { profile ->
            profile.providerId == com.google.firebase.auth.GoogleAuthProvider.PROVIDER_ID
        } ?: false
        viewModel.openPasswordDialog = true
    }, enabled = { _, context -> checkInternetConnection(context) }),
    EditLocation(SettingsTypeEnum.MenuLink, {
        Icon(
            Icons.Default.EditLocation,
            contentDescription = "Change Location"
        )
    }, "Edit Location", "Update your current location", { viewModel ->
        viewModel.openLocationDialog = true
    }, { viewModel ->
        val context = LocalContext.current
        if (!viewModel.addressUser.isNullOrEmpty()) {
            val address = viewModel.addressUser!!.split(" ")
            IconButton(onClick = {
                viewModel.openMap(
                    address[0].toDouble(),
                    address[1].toDouble(),
                    context
                )
            }) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "open map"
                )
            }
        }
    }, enabled = { _, context -> checkInternetConnection(context) }),
    BiometricLogin(
        SettingsTypeEnum.Switch,
        {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null
            )
        },
        "Biometric Login protection",
        "Protect the app with biometrics when opening it",
        onCheckedChange = { viewModel, context, state ->
            viewModel.toggleBiometrics(context, state)
        },
        enabled = { viewModel, context ->
            viewModel.isBiometricAvailable(context) == BiometricManager.BIOMETRIC_SUCCESS
        },
        switchState = {
            rememberPreferenceDataStoreBooleanSettingState(
                key = "biometric",
                defaultValue = false
            )
        }),
    Notification(SettingsTypeEnum.MenuLink, {
        Icon(
            Icons.Default.Notifications,
            contentDescription = "Enable Notifications"
        )
    }, "Notifications", "Enable notifications", { viewModel ->
        viewModel.goToNotificationSettings()
    }),
    Logout(SettingsTypeEnum.MenuLink, {
        Icon(
            Icons.Default.Logout,
            contentDescription = "Logout",
            tint = MaterialTheme.colorScheme.error
        )
    }, "Logout", "", { viewModel ->
        viewModel.openLogoutDialog = true
    })
}

enum class SettingsTypeEnum {
    MenuLink, Switch
}