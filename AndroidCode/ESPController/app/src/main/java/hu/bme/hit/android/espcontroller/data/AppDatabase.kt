package hu.bme.hit.android.espcontroller.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Database(
    entities = [Rooms::class, Devices::class, Pins::class], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entitiesDao(): EntitiesDAO

    companion object {
        private var Instance: AppDatabase? = null
        private var pinsLoaded = false

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                if (!pinsLoaded) {
                    CoroutineScope(Dispatchers.IO).launch {
                        loadPins(context)
                    }
                    pinsLoaded = true
                }

                Room.databaseBuilder(context, AppDatabase::class.java, "rooms_database")
                    .fallbackToDestructiveMigration().build().also { Instance = it }
            }
        }

        private suspend fun loadPins(context: Context) {
            val database = getDatabase(context)
            val dao = database.entitiesDao()

            if (dao.getAllPins().isEmpty()) {
                try {
                    val inputStream = context.assets.open("pinsRefined.txt")
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        val parts = line!!.split(",")
                        val pin = Pins(
                            pinNumber = parts[0].toInt(),
                            direction = enumValueOf(parts[1]),
                            sign = enumValueOf(parts[2])
                        )
                        dao.insert(pin)
                    }

                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}