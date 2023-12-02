package hu.bme.hit.android.espcontroller.screen.common

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.bme.hit.android.espcontroller.R
import hu.bme.hit.android.espcontroller.data.Direction
import hu.bme.hit.android.espcontroller.data.Pins
import hu.bme.hit.android.espcontroller.data.deviceTypes
import hu.bme.hit.android.espcontroller.network.NetworkHandler
import hu.bme.hit.android.espcontroller.screen.EntitiesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import hu.bme.hit.android.espcontroller.data.Sign as Sign

@Composable
fun TimeDialog(
    networkHandler: NetworkHandler, onSetTime: (Int) -> Unit, onDismiss: () -> Unit
) {
    val selectedValue = remember { mutableStateOf("") }

    val isSelectedItem: (String) -> Boolean = { selectedValue.value == it }
    val onChangeState: (String) -> Unit = { selectedValue.value = it }

    networkHandler.sendMessage("getTime")
    val timeStamps = networkHandler.getTimeStamps()
    LaunchedEffect(Unit) {
        while (true) {
            val msg = NetworkHandler.getMessageChannel().receive()
            //Log.d("TimeDialog", "msg: $msg")

            if (msg in timeStamps) {
                onChangeState(msg)
            }
        }
    }

    AlertDialog(containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.set_time),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(Modifier.padding(8.dp)) {
                networkHandler.getTimeStamps().forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .selectable(
                                selected = isSelectedItem(item),
                                onClick = { onChangeState(item) },
                                role = Role.RadioButton
                            )
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.primary
                            ), selected = isSelectedItem(item), onClick = null
                        )
                        Text(
                            text = item,
                            modifier = Modifier,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ), modifier = Modifier.fillMaxWidth(), onClick = {
                onSetTime(networkHandler.getTimeStamps().indexOf(selectedValue.value))
                onDismiss()
            }) {
                Text(text = stringResource(R.string.save_time))
            }
        },
        dismissButton = {
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ), modifier = Modifier.fillMaxWidth(), onClick = {
                onDismiss()
            }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun AddRoomDialog(
    onAddRoom: (String) -> Unit, onDismiss: () -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_room),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            TextField(
                singleLine = true,
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text(text = stringResource(R.string.enter_name_of_new_room)) },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                ),
            )
        },
        confirmButton = {
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ), modifier = Modifier.fillMaxWidth(), onClick = {
                if (roomName.isEmpty()) {
                    Toast.makeText(context, R.string.input_field_is_empty, Toast.LENGTH_SHORT)
                        .show()
                    return@Button
                }
                onAddRoom(roomName)
                onDismiss()
            }) {
                Text(text = stringResource(R.string.save_room))
            }
        },
        dismissButton = {
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ), modifier = Modifier.fillMaxWidth(), onClick = {
                onDismiss()
            }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun AddDeviceDialog(
    onAddDevice: (String, String, Sign, Int) -> Unit,
    onDismiss: () -> Unit,
    entitiesViewModel: EntitiesViewModel = viewModel(factory = EntitiesViewModel.factory)
) {
    val context = LocalContext.current
    var deviceName by remember { mutableStateOf( "") }

    //RadioButtons
    val (selectedDeviceType, onDeviceTypeSelected) = remember {
        mutableStateOf<String?>(
              null
        )
    }
    val (selectedSignType, onSignTypeSelected) = remember { mutableStateOf<String?>( null) }

    //Dropdown menu
    var pinsList: List<Pins> by remember { mutableStateOf(listOf()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedPin by remember { mutableStateOf<Pins?>( null) }

    AlertDialog(containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_device),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                TextField(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    singleLine = true,
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text(text = stringResource(R.string.enter_name_of_new_device)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    ),
                )
                //deviceTypeRadioBtn
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = stringResource(R.string.device),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                    ) {
                        deviceTypes.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.primary
                                ), selected = (type.key == selectedDeviceType), onClick = {
                                    onDeviceTypeSelected(type.key)
                                })
                                Text(
                                    text = type.key,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                //signTypeRadioBtn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = stringResource(R.string.sign),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    var signSelected = false
                    Sign.values().forEach { sign ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(enabled = if (selectedDeviceType != null) {
                                deviceTypes[selectedDeviceType]?.second?.contains(sign)!!
                            } else false, colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.primary
                            ), selected = if (selectedDeviceType != null) {
                                if (deviceTypes[selectedDeviceType]?.second?.contains(sign)!! && sign.name == selectedSignType) {
                                    signSelected = true
                                    true
                                } else false
                            } else false, onClick = {
                                onSignTypeSelected(sign.name)
                            })
                            Text(
                                text = sign.name,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    LaunchedEffect(selectedSignType) {
                        if (selectedDeviceType != null && selectedSignType != null) {
                            val (newPinsList, newSelectedPin) = reloadPinsList(
                                entitiesViewModel,
                                Direction.valueOf(deviceTypes[selectedDeviceType]?.first?.name!!),
                                Sign.valueOf(selectedSignType)
                            )
                            pinsList = newPinsList
                            selectedPin = newSelectedPin
                        }
                    }

                    if (!signSelected) onSignTypeSelected(null)
                }
                //ExposedDropdownMenuBox
                if (selectedDeviceType != null && selectedSignType != null) {
                    LaunchedEffect(entitiesViewModel) {
                        val (newPinsList, newSelectedPin) = reloadPinsList(
                            entitiesViewModel,
                            Direction.valueOf(deviceTypes[selectedDeviceType]?.first?.name!!),
                            Sign.valueOf(selectedSignType)
                        )
                        pinsList = newPinsList
                        selectedPin = newSelectedPin
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
                            expanded = !expanded
                        }) {
                            TextField(
                                value = selectedPin?.pinNumber?.toString() ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                pinsList.forEach { pin ->
                                    DropdownMenuItem(text = {
                                        Text(
                                            text = pin.pinNumber.toString(),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }, onClick = {
                                        selectedPin = pin
                                        expanded = false
                                    })
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ), modifier = Modifier.fillMaxWidth(), onClick = {
                if (deviceName.isEmpty() || selectedDeviceType == null || selectedSignType == null || selectedPin == null) {
                    Toast.makeText(
                        context, R.string.one_input_field_is_empty, Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                onAddDevice(
                    deviceName,
                    selectedDeviceType,
                    Sign.valueOf(selectedSignType),
                    selectedPin!!.pinNumber
                )

                onDismiss()
            }) {
                Text(text = stringResource(R.string.save_device))
            }
        },
        dismissButton = {
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ), modifier = Modifier.fillMaxWidth(), onClick = {
                onDismiss()
            }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

private suspend fun reloadPinsList(
    entitiesViewModel: EntitiesViewModel, selectedOptionDir: Direction?, selectedOptionSign: Sign?
): Pair<List<Pins>, Pins?> {
    val loadedPinsList = withContext(Dispatchers.IO) {
        entitiesViewModel.getAvailablePins(
            selectedOptionDir!!, selectedOptionSign!!
        )
    }
    return loadedPinsList to loadedPinsList.firstOrNull()
}