package hu.bme.hit.android.espcontroller.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import hu.bme.hit.android.espcontroller.R
import hu.bme.hit.android.espcontroller.network.NetworkHandler
import hu.bme.hit.android.espcontroller.screen.common.TopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StartScreen(
    navController: NavController, networkHandler: NetworkHandler
) {
    var actionEnabled by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var ipAddress by remember { mutableStateOf("") }

    TopAppBar(
        navController = navController,
        networkHandler = networkHandler,
        screenName = stringResource(id = R.string.app_name)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
        ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = actionEnabled,
            modifier = Modifier.padding(16.dp),
            singleLine = true,
            value = ipAddress,
            label = { Text(text = stringResource(R.string.enter_ip_address)) },
            onValueChange = { ipAddress = it })

        Button(
            enabled = actionEnabled,
            onClick = {
                ipAddress = ipAddress.trim()
                if (networkHandler.isPrivateIP(ipAddress)) {
                    coroutineScope.launch {
                        actionEnabled = false
                        networkHandler.initialization(ipAddress)
                        delay(5000)
                        if (networkHandler.isWebSocketOpen()) {
                            navController.navigate("Rooms")
                        }
                        else {
                            Toast.makeText(
                                context, R.string.ip_address_is_offline, Toast.LENGTH_SHORT
                            ).show()
                        }
                        actionEnabled = true
                    }
                } else if (ipAddress.trim().isNotEmpty()) {
                    Toast.makeText(context, R.string.invalid_ip_address, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.input_field_is_empty, Toast.LENGTH_SHORT)
                        .show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.connect), color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}