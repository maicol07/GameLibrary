package it.unibo.gamelibrary.ui.views.BiometricLock

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.interfaces.HasBiometrics
import it.unibo.gamelibrary.ui.views.destinations.HomeDestination
import javax.inject.Inject

@HiltViewModel
class BiometricLockScreenViewModel @Inject constructor() : ViewModel(), HasBiometrics {
    fun authenticate(context: Context, navController: NavController) {
        showBiometricPrompt(context, "Unlock GameLibrary", onSuccess = {
            navController.navigate(HomeDestination) {
                popUpTo(navController.currentDestination!!.route!!) {
                    inclusive = true
                }
            }
        })
    }
}