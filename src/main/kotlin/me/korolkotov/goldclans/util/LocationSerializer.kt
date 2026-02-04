package me.korolkotov.goldclans.util

import org.bukkit.Bukkit
import org.bukkit.Location

object LocationSerializer {
    fun serialize(location: Location?): String? {
        if (location == null) return null

        var string = StringBuilder()
        string = string.append(location.world.name).append(';')
        string = string.append(location.x.toString()).append(';')
        string = string.append(location.y.toString()).append(';')
        string = string.append(location.z.toString()).append(';')
        string = string.append(location.yaw.toString()).append(';')
        string = string.append(location.pitch.toString())
        return string.toString()
    }

    fun deserialize(data: String?): Location? {
        if (data == null) return null

        val args = data.split(';')
        val world = Bukkit.getWorld(args[0])!!
        val x = args[1].toDouble()
        val y = args[2].toDouble()
        val z = args[3].toDouble()
        val yaw = args[4].toFloat()
        val pitch = args[5].toFloat()
        return Location(world, x, y, z).apply {
            this.yaw = yaw
            this.pitch = pitch
        }
    }
}