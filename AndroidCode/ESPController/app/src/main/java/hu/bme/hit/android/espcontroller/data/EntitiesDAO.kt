package hu.bme.hit.android.espcontroller.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntitiesDAO {
    @Query("SELECT * FROM rooms")
    fun getAllRooms(): Flow<List<Rooms>>

    @Query("SELECT name FROM rooms WHERE roomID = :roomID")
    fun getRoomName(roomID: Long): String

    @Query("SELECT * FROM devices WHERE deviceID = :deviceID")
    fun getDevice(deviceID: Long): Devices

    @Query("SELECT COUNT(*) FROM devices WHERE roomID = :roomID")
    fun countDevices(roomID: Long): Long

    @Query("SELECT * FROM devices WHERE roomID = :roomID")
    fun getAllDevices(roomID: Long): Flow<List<Devices>>

    @Query("SELECT * FROM pins")
    fun getAllPins(): List<Pins>

    @Query("SELECT * FROM pins WHERE direction = :dir AND sign = :sign AND pinNumber NOT IN (SELECT pinNumber FROM devices)")
    fun getAvailablePins(dir: Direction, sign: Sign): List<Pins>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(room: Rooms)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(devices: Devices)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pins: Pins)

    @Delete
    suspend fun deleteRoom(rooms: Rooms)

    @Delete
    suspend fun deleteDev(devices: Devices)

    @Query("DELETE FROM devices WHERE roomID = :roomID")
    suspend fun deleteDevs(roomID: Long)
}