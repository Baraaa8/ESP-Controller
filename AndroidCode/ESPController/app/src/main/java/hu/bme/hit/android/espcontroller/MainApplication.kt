package hu.bme.hit.android.espcontroller

import android.app.Application
import hu.bme.hit.android.espcontroller.data.AppDatabase

class MainApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}