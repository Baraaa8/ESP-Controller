package hu.bme.hit.android.espcontroller.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import hu.bme.hit.android.espcontroller.R
import hu.bme.hit.android.espcontroller.data.Devices
import hu.bme.hit.android.espcontroller.network.NetworkHandler
import hu.bme.hit.android.espcontroller.screen.common.AddDeviceDialog
import hu.bme.hit.android.espcontroller.screen.common.TopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevicesScreen(
    navController: NavController,
    networkHandler: NetworkHandler,
    roomID: Long,
    entitiesViewModel: EntitiesViewModel = viewModel(factory = EntitiesViewModel.factory)
) {
    val context = LocalContext.current
    val devicesList by entitiesViewModel.getAllDevices(roomID).collectAsState(emptyList())
    val lazyListState = rememberLazyListState()
    var isAddDeviceDialogOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var roomName by remember { mutableStateOf("") }

    LaunchedEffect(roomID) {
        val name = withContext(Dispatchers.IO) {
            entitiesViewModel.getRoomName(roomID)
        }

        roomName = if (name.endsWith("s")) "$name' ${context.getString(R.string.devices)}"
        else "$name${context.getString(R.string.s_devices)}"
    }

    Scaffold(topBar = {
        TopAppBar(
            navController = navController, networkHandler = networkHandler, screenName = roomName
        )
    }, content = { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), state = lazyListState
        ) {
            items(items = devicesList,
                key = { device -> device.deviceID },
                itemContent = { device ->
                    val dismissState = rememberDismissState(initialValue = DismissValue.Default,
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart)
                                coroutineScope.launch {
                                    entitiesViewModel.deleteDev(device)
                                }
                            true
                        },
                        positionalThreshold = { swipeActivationFloat -> swipeActivationFloat / 3 })
                    SwipeToDismiss(
                        state = dismissState,
                        modifier = Modifier.animateItemPlacement(),
                        background = {
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    DismissValue.DismissedToStart -> Color.Red
                                    else -> Color.Transparent
                                }, label = stringResource(R.string.swipebackgrounddev)
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(10.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        dismissContent = {
                            ElevatedCard(
                                onClick = { navController.navigate("${roomID}/Devices/${device.deviceID}") },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .height(100.dp)
                                    .padding(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            device.name.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.type_pin,
                                                device.type,
                                                device.pinNumber
                                            ),
                                            fontWeight = FontWeight.Light,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = stringResource(id = R.string.arrow),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        },
                        directions = setOf(DismissDirection.EndToStart)
                    )
                })
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { isAddDeviceDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_device),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    })

    if (isAddDeviceDialogOpen) {
        AddDeviceDialog(onAddDevice = { deviceName, type, sign, pinNumber ->
            coroutineScope.launch {
                val newDevice = Devices(
                    name = deviceName,
                    type = type,
                    sign = sign,
                    pinNumber = pinNumber,
                    roomID = roomID
                )
                entitiesViewModel.addDevice(newDevice)
                isAddDeviceDialogOpen = false
            }
        }, onDismiss = { isAddDeviceDialogOpen = false }
        )
    }
}
