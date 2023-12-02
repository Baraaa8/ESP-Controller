package hu.bme.hit.android.espcontroller.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import hu.bme.hit.android.espcontroller.R
import hu.bme.hit.android.espcontroller.data.Devices
import hu.bme.hit.android.espcontroller.data.Sign
import hu.bme.hit.android.espcontroller.network.NetworkHandler
import hu.bme.hit.android.espcontroller.screen.common.TopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ControlScreen(
    navController: NavController,
    networkHandler: NetworkHandler,
    deviceID: Long,
    entitiesViewModel: EntitiesViewModel = viewModel(factory = EntitiesViewModel.factory)
) {
    var device by remember { mutableStateOf<Devices?>(null) }

    LaunchedEffect(deviceID) {
        val newDevice = withContext(Dispatchers.IO) {
            entitiesViewModel.getDevice(deviceID)
        }

        device = newDevice
    }


    if (device != null) {
        TopAppBar(
            navController = navController,
            networkHandler = networkHandler,
            screenName = if (device!!.name.endsWith("s")) stringResource(
                R.string.control,
                device!!.name
            ) else stringResource(R.string.s_control, device!!.name)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (device!!.sign == Sign.ANALOG) AnalogElements(device!!, networkHandler)
            else DigitalElements(device!!, networkHandler)
        }
    }
}

@Composable
private fun AnalogElements(device: Devices, networkHandler: NetworkHandler) {
    var sliderPosition by remember { mutableStateOf<Float?>(null) }

    val stepsNumber = if (device.type == "SERVO") 1 else 3
    val rangeNumber = if (device.type == "SERVO") 180f else 252f

    networkHandler.sendMessage("getPos ${device.pinNumber}")

    LaunchedEffect(Unit) {
        while (true) {
            val msg = NetworkHandler.getMessageChannel().receive()

            if (msg.matches(Regex("${device.pinNumber} [0-9]+"))) {
                sliderPosition = msg.split(" ")[1].toFloat()
            }
        }
    }

    if (sliderPosition != null) {
        Slider(
            value = sliderPosition!!, onValueChange = {
                sliderPosition = it
                networkHandler.sendMessage("${device.pinNumber} ${sliderPosition!!.toInt()}")
            }, colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ), steps = stepsNumber, valueRange = 0f..rangeNumber
        )
        Text(text = sliderPosition.toString(), color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun DigitalElements(device: Devices, networkHandler: NetworkHandler) {
    var isOn by remember { mutableStateOf<Boolean?>(null) }

    networkHandler.sendMessage("isON ${device.pinNumber}")

    LaunchedEffect(Unit) {
        while (true) {
            val msg = NetworkHandler.getMessageChannel().receive()

            if (msg.matches(Regex("${device.pinNumber} (ON|OFF)"))) {
                isOn = msg.split(" ")[1] == "ON"
            }
        }
    }

if (isOn != null) {
    Image(
        painter = painterResource(id = if (isOn!!) R.drawable.power_on else R.drawable.power_off),
        contentDescription = stringResource(R.string.power_img),
        modifier = Modifier.clickable {
            isOn = !isOn!!
            networkHandler.sendMessage(if (isOn!!) "setON ${device.pinNumber}" else "setOFF ${device.pinNumber}")
        },
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
    )

    Text(text = if (isOn!!) stringResource(R.string.on) else stringResource(R.string.off), color = MaterialTheme.colorScheme.onBackground)
}
}