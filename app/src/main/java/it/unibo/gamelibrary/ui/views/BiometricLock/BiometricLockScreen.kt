package it.unibo.gamelibrary.ui.views.BiometricLock

import android.hardware.biometrics.BiometricManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.unibo.gamelibrary.utils.TopAppBarState


@Composable
fun BiometricLockScreen(
    viewModel: BiometricLockScreenViewModel = hiltViewModel(),
    locked: MutableState<Boolean>
) {
    TopAppBarState.hide = true
    val context = LocalContext.current
    if (viewModel.isBiometricAvailable(context) == BiometricManager.BIOMETRIC_SUCCESS) {
        viewModel.authenticate(context, locked)
    } else {
        Toast.makeText(context, "Biometric authentication not available", Toast.LENGTH_SHORT).show()
        locked.value = false
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.Center
    ) {
        FilledTonalIconButton(
            onClick = { viewModel.authenticate(context, locked) },
            modifier = Modifier
                .size(128.dp)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = "The app is locked with biometric protection. Please authenticate to unlock it.",
            textAlign = TextAlign.Justify
        )
    }
}