package it.unibo.gamelibrary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import cat.ereza.customactivityoncrash.config.CaocConfig
import it.unibo.gamelibrary.ui.common.components.CustomDialog
import it.unibo.gamelibrary.ui.theme.GameLibraryTheme
import it.unibo.gamelibrary.utils.findActivity

private lateinit var config: CaocConfig
private lateinit var errorInformation: String
private var showDetailsDialog by mutableStateOf(false)

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val c = CustomActivityOnCrash.getConfigFromIntent(intent)
        if (c == null) {
            //This should never happen - Just finish the activity to avoid a recursive crash.
            Log.d("CrashActivity", "No config found. Recovering from crash.")
            finish()
            return
        }
        config = c

        errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, intent)

        setContent {
            GameLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    CrashScreen()
                }
            }
        }
    }
}

@Composable
fun CrashScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Text("An unexpected error occurred. Please restart the app.")
        val restart =
            remember {
                @Suppress("KotlinConstantConditions")
                config.isShowRestartButton && config.restartActivityClass != null
            }
        val context = LocalContext.current
        Button(onClick = {
            if (restart) {
                CustomActivityOnCrash.restartApplication(context.findActivity(), config)
            } else {
                CustomActivityOnCrash.closeApplication(context.findActivity(), config)
            }
        }) {
            Icon(Icons.Default.RestartAlt, contentDescription = null)
            Text(text = "${if (restart) "Restart" else "Close"} app")
        }
        val showErrorDetails = remember { config.isShowErrorDetails }
        if (showErrorDetails) {
            TextButton(onClick = {
                showDetailsDialog = true
            }) {
                Text("Show error details")
            }
        }
        if (showDetailsDialog) {
            CrashStackDialog()
        }
    }
}

@Composable
fun CrashStackDialog() {
    val context = LocalContext.current
    CustomDialog(
        onDismissRequest = { showDetailsDialog = false },
        title = { Text("Error details") },
        buttons = {
            TextButton(onClick = {
                copyErrorToClipboard(context)
            }) {
                Text("Copy error to clipboard")
            }

            TextButton(onClick = {
                showDetailsDialog = false
            }) {
                Text("Close")
            }
        }) {
        Column(Modifier.verticalScroll(remember { ScrollState(0) })) {
            Text(errorInformation, fontFamily = FontFamily.Monospace)
        }
    }

}

fun copyErrorToClipboard(context: Context) {
    val clipboard: ClipboardManager? = context.getSystemService(ClipboardManager::class.java)

    //Are there any devices without clipboard...?
    if (clipboard == null) {
        Toast.makeText(context, "Clipboard not available", Toast.LENGTH_SHORT).show()
        return
    }
    val clip = ClipData.newPlainText("Error information", errorInformation)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied error information to clipboard!", Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
fun CrashScreenPreview() {
    previewConfig()
    GameLibraryTheme {
        CrashScreen()
    }
}

fun previewConfig() {
    config = CaocConfig.Builder.create().apply {
        showErrorDetails(true)
        showRestartButton(true)
        restartActivity(CrashActivity::class.java)
    }.get()
    errorInformation = "This is a test error"
}

@Preview(showBackground = true)
@Composable
fun CrashScreenDialogPreview() {
    previewConfig()
    GameLibraryTheme {
        CrashStackDialog()
    }
}