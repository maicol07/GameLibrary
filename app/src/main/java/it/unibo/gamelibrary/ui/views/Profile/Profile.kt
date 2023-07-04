package it.unibo.gamelibrary.ui.views.Profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Destination
@Composable
fun Profile(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }

    Column() {
        Text(
            //text = "your surname is ${user.surname}"
            text = "profilo"
        )
        Button(onClick = { viewModel.reviewText = ""; showDialog = true }
        ) {
            Text(text = "Write a Review")
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { /*TODO*/ }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "sei in un dialog. spero")

                    TextField(
                        label = { Text(text = "Write a review!") },
                        value = viewModel.reviewText,
                        onValueChange = { viewModel.reviewText = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showDialog = false
                        },
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = "Done")
                    }
                }
            }
        }
    }
}

