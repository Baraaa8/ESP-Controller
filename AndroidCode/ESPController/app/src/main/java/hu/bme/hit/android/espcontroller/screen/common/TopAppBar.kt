package hu.bme.hit.android.espcontroller.screen.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import hu.bme.hit.android.espcontroller.R
import hu.bme.hit.android.espcontroller.network.NetworkHandler

@Composable
fun TopAppBar(
    navController: NavController, networkHandler: NetworkHandler, screenName: String?
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route.toString()
    var isTimeDialogOpen by remember { mutableStateOf(false) }
    var alreadyLogged by remember { mutableStateOf(false) }

    TopAppBar(title = {
        Text(
            screenName ?: "Loading...",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }, colors = TopAppBarDefaults.mediumTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary
    ), navigationIcon = {
        if (navController.previousBackStackEntry != null) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }, actions = {
        if (currentRoute != stringResource(R.string.app_name)) { //TODO on controller screen, delete as well
            if (!alreadyLogged) {
                alreadyLogged = true
            }

            IconButton(onClick = { isTimeDialogOpen = true }) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = stringResource(R.string.set_time),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else if (alreadyLogged) {
            alreadyLogged = false
            networkHandler.closeWebSocket()
        }
    })

    if (isTimeDialogOpen) {
        TimeDialog(networkHandler = networkHandler, onSetTime = {index ->
            networkHandler.sendMessage("setTime $index")
        }, onDismiss = { isTimeDialogOpen = false })
    }
}
