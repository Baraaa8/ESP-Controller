package hu.bme.hit.android.espcontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import hu.bme.hit.android.espcontroller.navigation.NavGraph
import hu.bme.hit.android.espcontroller.ui.theme.ESPControllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ESPControllerTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                  NavGraph()
                }
            }
        }
    }
}