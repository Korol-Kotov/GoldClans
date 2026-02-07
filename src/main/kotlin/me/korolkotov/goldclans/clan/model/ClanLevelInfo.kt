package me.korolkotov.goldclans.clan.model

import me.korolkotov.goldclans.config.ConfigManager
import org.bukkit.Material
import kotlin.random.Random

data class ClanLevelInfo(
    var levelCost: Double,
    var slotCost: Double,
    val resources: MutableMap<Material, Int>
) {
    companion object {
        fun getRandom(): ClanLevelInfo {
            val levelCost = ConfigManager.instance.config.clan.levels.cost.random().toDouble()
            val slotCost = ConfigManager.instance.config.clan.storage.cost.random().toDouble()
            val resources = ConfigManager.instance.config.clan.levels.resources.mapNotNull { string ->
                val args = string.split(' ')
                val material = Material.entries.firstOrNull { it.name.equals(args.getOrNull(0), true) } ?: return@mapNotNull null
                val amount = args.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                val chance = args.getOrNull(2)?.toDoubleOrNull() ?: return@mapNotNull null
                Triple(material, amount, chance)
            }
            return ClanLevelInfo(levelCost, slotCost, pickRandomItems(resources, 3).toMutableMap())
        }

        fun serialize(info: ClanLevelInfo): String {
            var string = StringBuilder()
            string = string.append(info.levelCost.toString()).append(';')
            string = string.append(info.slotCost.toString()).append(';')
            for ((type, amount) in info.resources) {
                string = string.append(type.name).append(':').append(amount.toString()).append(';')
            }
            return string.toString()
        }

        fun deserialize(string: String): ClanLevelInfo {
            val args = string.split(';')
            val levelCost = args[0].toDouble()
            val slotCost = args[1].toDouble()
            val resources = mutableMapOf<Material, Int>()
            for (i in 2..<args.size) {
                val info = args[i].split(':')
                val type = Material.entries.first { it.name.equals(info[0], true) }
                val amount = info[1].toInt()
                resources[type] = amount
            }
            return ClanLevelInfo(levelCost, slotCost, resources)
        }

        private fun pickRandomItems(source: List<Triple<Material, Int, Double>>, picks: Int): Map<Material, Int> {
            val result = mutableMapOf<Material, Int>()

            val totalWeight = source.sumOf { it.third }

            repeat(picks) {
                val roll = Random.nextDouble(totalWeight)
                var current = 0.0

                for ((material, amount, chance) in source) {
                    current += chance
                    if (roll <= current) {
                        result[material] = (result[material] ?: 0) + amount
                        break
                    }
                }
            }

            return result
        }
    }
}