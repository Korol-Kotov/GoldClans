package me.korolkotov.goldclans.clan.model

import org.bukkit.Material

data class ClanLevelInfo(
    var money: Int,
    val resources: MutableMap<Material, Int>
) {
    companion object {
        fun serialize(info: ClanLevelInfo): String {
            var string = StringBuilder()
            string = string.append(info.money.toString()).append(';')
            for ((type, amount) in info.resources) {
                string = string.append(type.name).append(':').append(amount.toString()).append(';')
            }
            return string.toString()
        }

        fun deserialize(string: String): ClanLevelInfo {
            val args = string.split(';')
            val money = args[0].toInt()
            val resources = mutableMapOf<Material, Int>()
            for (i in 1..<args.size) {
                val info = args[i].split(':')
                val type = Material.entries.first { it.name.equals(info[0], true) }
                val amount = info[1].toInt()
                resources[type] = amount
            }
            return ClanLevelInfo(money, resources)
        }
    }
}