package it.unibo.gamelibrary.ui.views.BiometricLock

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.interfaces.HasBiometrics
import it.unibo.gamelibrary.utils.TopAppBarState
import it.unibo.gamelibrary.utils.findActivity
import javax.inject.Inject

@HiltViewModel
class BiometricLockScreenViewModel @Inject constructor() : ViewModel(), HasBiometrics {
    var oldTopAppBarShowState = TopAppBarState.show

    fun authenticate(context: Context, locked: MutableState<Boolean>) {
        val lifecycle = context.findActivity().lifecycle
        val lifecycleEventObserver = LifecycleEventObserver { _, event ->
            if (event === Lifecycle.Event.ON_RESUME) {
                authenticate(context, locked)
            }
        }
        if (lifecycle.currentState !== Lifecycle.State.RESUMED) {
            lifecycle.addObserver(lifecycleEventObserver)
            return
        }

        lifecycle.removeObserver(lifecycleEventObserver)
        showBiometricPrompt(context, locked)
    }

    fun showBiometricPrompt(context: Context, locked: MutableState<Boolean>) {
        showBiometricPrompt(context, "Unlock GameLibrary", onSuccess = {
            locked.value = false
            TopAppBarState.show = oldTopAppBarShowState
        })
    }
}