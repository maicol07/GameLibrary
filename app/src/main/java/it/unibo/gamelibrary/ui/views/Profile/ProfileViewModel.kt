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

    var reviewText by mutableStateOf("")

    //var user: User
    //userName = database locale( Firebase.auth.currentUser.uid ) // per ora non c'è locale


}