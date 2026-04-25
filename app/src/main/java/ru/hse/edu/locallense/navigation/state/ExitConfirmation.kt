package ru.hse.edu.locallense.navigation.state

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import ru.hse.edu.locallense.R
import ru.hse.locallense.common.Core

private const val ARM_TIMEOUT_MS = 2_000L

@Composable
fun BackToExitConfirmation() {
    val context = LocalContext.current.applicationContext
    val toastMessage = stringResource(R.string.app_exit_confirmation)
    var armed by remember { mutableStateOf(false) }

    LaunchedEffect(armed) {
        if (armed) {
            delay(ARM_TIMEOUT_MS)
            armed = false
        }
    }

    BackHandler {
        if (armed) {
            context.startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        } else {
            armed = true
            Core.toaster.showToast(toastMessage)
        }
    }
}
