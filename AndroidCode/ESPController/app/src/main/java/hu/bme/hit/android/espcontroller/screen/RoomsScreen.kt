package hu.bme.hit.android.espcontroller.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import hu.bme.hit.android.espcontroller.R
import hu.bme.hit.android.espcontroller.data.Rooms
import hu.bme.hit.android.espcontroller.network.NetworkHandler
import hu.bme.hit.android.espcontroller.screen.common.AddRoomDialog
import hu.bme.hit.android.espcontroller.screen.common.TopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomsScreen(
    navController: NavController,
    networkHandler: NetworkHandler,
    entitiesViewModel: EntitiesViewModel = viewModel(factory = EntitiesViewModel.factory)
) {
    val roomsList by entitiesViewModel.getAllRooms().collectAsState(emptyList())
    val lazyListState = rememberLazyListState()
    var isAddRoomDialogOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = {
        TopAppBar(
            navController = navController,
            networkHandler = networkHandler,
            screenName = stringResource(id = R.string.rooms)
        )
    }, content = { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), state = lazyListState
        ) {
            items(items = roomsList, key = { room -> room.roomID }, itemContent = { room ->
                val dismissState = rememberDismissState(initialValue = DismissValue.Default,
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToStart) coroutineScope.launch {
                            entitiesViewModel.deleteDevs(room.roomID)
                            entitiesViewModel.deleteRoom(room)
                        }
                        true
                    },
                    positionalThreshold = { swipeActivationFloat -> swipeActivationFloat / 3 })
                SwipeToDismiss(state = dismissState,
                    modifier = Modifier.animateItemPlacement(),
                    background = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                DismissValue.DismissedToStart -> Color.Red
                                else -> Color.Transparent
                            }, label = stringResource(R.string.swipebackgroundroom)
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
                            onClick = { navController.navigate("${room.roomID}/Devices") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = room.name.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                val deviceNumber = remember { mutableLongStateOf(0) }
                                LaunchedEffect(room.roomID) {
                                    val result = withContext(Dispatchers.IO) { entitiesViewModel.countDevices(room.roomID) }
                                    deviceNumber.longValue = result
                                }
                                Text(
                                    text = if (deviceNumber.longValue < 2) stringResource(
                                        R.string.counted_device,
                                        deviceNumber.longValue
                                    ) else stringResource(
                                        R.string.counted_devices,
                                        deviceNumber.longValue
                                    ),
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    },
                    directions = setOf(DismissDirection.EndToStart)
                )
            })
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { isAddRoomDialogOpen = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_room),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    })

    if (isAddRoomDialogOpen) {
        AddRoomDialog(onAddRoom = { roomName ->
            coroutineScope.launch {
                val newRoom = Rooms(name = roomName)
                entitiesViewModel.addRoom(newRoom)
                isAddRoomDialogOpen = false
            }
        }, onDismiss = { isAddRoomDialogOpen = false })
    }
}
