package me.korolkotov.goldclans.clan.model

import me.korolkotov.goldclans.config.ConfigManager
import org.bukkit.Material

data class ClanLevelInfo(
    var levelCost: Int,
    var slotCost: Int,
    val resources: MutableList<Triple<Material, Int, Double>>
) {
    companion object {
        fun getRandom(): ClanLevelInfo {
            val levelCost = ConfigManager.instance.config.clan.levels.cost.random()
            val slotCost = ConfigManager.instance.config.clan.storage.cost.random()
            val resources = ConfigManager.instance.config.clan.levels.resources.mapNotNull { string ->
                val args = string.split(' ')
                val material = Material.entries.firstOrNull { it.name.equals(args.getOrNull(0), true) } ?: return@mapNotNull null
                val amount = args.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                val chance = args.getOrNull(2)?.toDoubleOrNull() ?: return@mapNotNull null
                Triple(material, amount, chance)
            }
            return ClanLevelInfo(levelCost, slotCost, resources.toMutableList())
        }

        fun serialize(info: ClanLevelInfo): String {
            var string = StringBuilder()
            string = string.append(info.levelCost.toString()).append(';')
            string = string.append(info.slotCost.toString()).append(';')
            for ((type, amount, chance) in info.resources) {
                string = string.append(type.name).append(':').append(amount.toString()).append(':').append(chance.toString()).append(';')
            }
            return string.toString()
        }

        fun deserialize(string: String): ClanLevelInfo {
            val args = string.split(';')
            val levelCost = args[0].toInt()
            val slotCost = args[1].toInt()
            val resources = mutableListOf<Triple<Material, Int, Double>>()
            for (i in 2..<args.size) {
                val info = args[i].split(':')
                val type = Material.entries.first { it.name.equals(info[0], true) }
                val amount = info[1].toInt()
                val chance = info[2].toDouble()
                resources.add(Triple(type, amount, chance))
            }
            return ClanLevelInfo(levelCost, slotCost, resources)
        }
    }
}