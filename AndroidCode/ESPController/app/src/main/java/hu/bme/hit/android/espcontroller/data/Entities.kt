package hu.bme.hit.android.espcontroller.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Direction {
    INPUT, OUTPUT
}

enum class Sign {
    ANALOG, DIGITAL
}

val deviceTypes = mapOf(
    "LED" to Pair(Direction.OUTPUT, listOf(Sign.ANALOG, Sign.DIGITAL)),
    "SERVO" to Pair(Direction.OUTPUT, listOf(Sign.ANALOG))
)

@Entity(tableName = "rooms")
data class Rooms(
    @PrimaryKey(autoGenerate = true) val roomID: Long = 0, val name: String
)

@Entity(tableName = "devices")
data class Devices(
    @PrimaryKey(autoGenerate = true) val deviceID: Long = 0,
    val name: String,
    val type: String,
    val sign: Sign,
    val pinNumber: Int,
    val roomID: Long
)

@Entity(tableName = "pins", primaryKeys = ["pinNumber", "direction", "sign"])
data class Pins(
    val pinNumber: Int, val direction: Direction, val sign: Sign
)