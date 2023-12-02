package hu.bme.hit.android.espcontroller.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import hu.bme.hit.android.espcontroller.MainApplication
import hu.bme.hit.android.espcontroller.data.Devices
import hu.bme.hit.android.espcontroller.data.Direction
import hu.bme.hit.android.espcontroller.data.EntitiesDAO
import hu.bme.hit.android.espcontroller.data.Pins
import hu.bme.hit.android.espcontroller.data.Rooms
import hu.bme.hit.android.espcontroller.data.Sign
import kotlinx.coroutines.flow.Flow

class EntitiesViewModel(
    private val entitiesDao: EntitiesDAO
) : ViewModel() {

    fun getAllRooms(): Flow<List<Rooms>> {
        return entitiesDao.getAllRooms()
    }

    fun getAllDevices(roomID: Long): Flow<List<Devices>> {
        return entitiesDao.getAllDevices(roomID)
    }

    fun getRoomName(roomID: Long): String {
        return entitiesDao.getRoomName(roomID)
    }

    fun getDevice(deviceID: Long): Devices {
        return entitiesDao.getDevice(deviceID)
    }

    fun countDevices(roomID: Long): Long {
        return entitiesDao.countDevices(roomID)
    }

    fun getAvailablePins(dir: Direction, sign: Sign): List<Pins> {
        return entitiesDao.getAvailablePins(dir, sign)
    }

    suspend fun addRoom(rooms: Rooms) {
        entitiesDao.insert(rooms)
    }

    suspend fun addDevice(devices: Devices) {
        entitiesDao.insert(devices)
    }

    suspend fun deleteRoom(rooms: Rooms) {
        entitiesDao.deleteRoom(rooms)
    }

    suspend fun deleteDev(devices: Devices) {
        entitiesDao.deleteDev(devices)
    }

    suspend fun deleteDevs(roomID: Long) {
        entitiesDao.deleteDevs(roomID)
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MainApplication)
                EntitiesViewModel(entitiesDao = application.database.entitiesDao())
            }
        }
    }
}