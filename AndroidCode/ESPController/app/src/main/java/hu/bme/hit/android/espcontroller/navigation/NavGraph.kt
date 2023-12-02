package hu.bme.hit.android.espcontroller.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.bme.hit.android.espcontroller.R
import hu.bme.hit.android.espcontroller.network.NetworkHandler
import hu.bme.hit.android.espcontroller.screen.ControlScreen
import hu.bme.hit.android.espcontroller.screen.DevicesScreen
import hu.bme.hit.android.espcontroller.screen.RoomsScreen
import hu.bme.hit.android.espcontroller.screen.StartScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val networkHandler = NetworkHandler

    NavHost(
        navController = navController, startDestination = stringResource(id = R.string.app_name)
    ) {
        composable("ESP Controller") {
            StartScreen(
                navController = navController, networkHandler = networkHandler
            )
        }
        composable("Rooms") {
            RoomsScreen(
                navController = navController, networkHandler = networkHandler
            )
        }
        composable("{roomID}/Devices") { backStackEntry ->
            DevicesScreen(
                navController = navController,
                networkHandler = networkHandler,
                roomID = backStackEntry.arguments?.getString("roomID")!!.toLong()
            )
        }
        composable("{roomID}/Devices/{deviceID}") { backStackEntry ->
            ControlScreen(
                navController = navController,
                networkHandler = networkHandler,
                deviceID = backStackEntry.arguments?.getString("deviceID")!!.toLong()
            )
        }
    }
}