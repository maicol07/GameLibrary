package it.unibo.gamelibrary.ui.views.Profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.unibo.gamelibrary.data.model.User
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
): ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    //var user: User
    var username = auth.currentUser?.displayName


    //maybe move this feature to gameView
    var reviewText by mutableStateOf("")
}