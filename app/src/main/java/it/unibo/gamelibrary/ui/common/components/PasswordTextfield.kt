package it.unibo.gamelibrary.ui.common.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PasswordTextfield(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    isHidden: Boolean,
    onPasswordVisible: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
){
    val focusManager = LocalFocusManager.current
    TextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isHidden) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        leadingIcon = { if (leadingIcon == null)
            Icon(
                Icons.Outlined.Lock,
                contentDescription = "password"
            ) else leadingIcon.invoke()
        },
        trailingIcon = {
            IconButton(onClick = {
                onPasswordVisible.invoke()
            }) {
                Icon(
                    imageVector = if (isHidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (isHidden) "hide password" else "show password"
                )
            }
        },
        isError = isError,
        supportingText = supportingText
    )
}