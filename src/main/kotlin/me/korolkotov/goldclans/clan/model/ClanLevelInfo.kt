package me.korolkotov.goldclans.clan.model

import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.util.toRange
import org.bukkit.Material
import kotlin.random.Random

data class ClanLevelInfo(
    var levelCost: Double,
    var slotCost: Double,
    val levelResources: MutableMap<Material, Int>,
    val slotsResources: MutableMap<Material, Int>
) {
    companion object {
        fun getRandom(): ClanLevelInfo {
            val levelCost = ConfigManager.instance.config.clan.levels.cost.random().toDouble()
            val slotCost = ConfigManager.instance.config.clan.storage.cost.random().toDouble()
            val levelResources = ConfigManager.instance.config.clan.levels.resources
            val slotsResources = ConfigManager.instance.config.clan.storage.resources
            return ClanLevelInfo(
                levelCost,
                slotCost,
                pickRandomItems(levelResources, ConfigManager.instance.config.clan.levels.pickResources.random()).toMutableMap(),
                pickRandomItems(slotsResources, ConfigManager.instance.config.clan.storage.pickResources.random()).toMutableMap()
            )
        }

        fun serialize(info: ClanLevelInfo): String {
            var string = StringBuilder()
            string = string.append(info.levelCost.toString()).append(';')
            for ((type, amount) in info.levelResources) {
                string = string.append(type.name).append(':').append(amount.toString()).append(';')
            }
            string = string.append('!')
            string = string.append(info.slotCost.toString()).append(';')
            for ((type, amount) in info.slotsResources) {
                string = string.append(type.name).append(':').append(amount.toString()).append(';')
            }
            return string.toString()
        }

        fun deserialize(string: String): ClanLevelInfo {
            val args = string.split('!')
            val levelArgs = args[0].split(';')
            val levelCost = levelArgs[0].toDouble()
            val levelResources = mutableMapOf<Material, Int>()
            for (i in 1..<levelArgs.size) {
                if (levelArgs[i].trim().isEmpty()) continue
                val info = levelArgs[i].split(':')
                val type = Material.entries.firstOrNull { it.name.equals(info.getOrNull(0), true) } ?: continue
                val amount = info.getOrNull(1)?.toIntOrNull() ?: continue
                levelResources[type] = amount
            }

            val slotsArgs = args[1].split(';')
            val slotCost = slotsArgs[0].toDouble()
            val slotsResources = mutableMapOf<Material, Int>()
            for (i in 1..<args.size) {
                if (slotsArgs[i].trim().isEmpty()) continue
                val info = slotsArgs[i].split(':')
                val type = Material.entries.firstOrNull { it.name.equals(info.getOrNull(0), true) } ?: continue
                val amount = info.getOrNull(1)?.toIntOrNull() ?: continue
                slotsResources[type] = amount
            }
            return ClanLevelInfo(levelCost, slotCost, levelResources, slotsResources)
        }

        private fun pickRandomItems(resources: List<String>, picks: Int): Map<Material, Int> {
            val pool = resources.mapNotNull { string ->
                val args = string.split(' ')
                val material = Material.entries
                    .firstOrNull { it.name.equals(args.getOrNull(0), true) }
                    ?: return@mapNotNull null

                val amount = args.getOrNull(1)?.toRange()?.random() ?: return@mapNotNull null
                val chance = args.getOrNull(2)?.toDoubleOrNull() ?: return@mapNotNull null

                Triple(material, amount, chance)
            }.toMutableList()

            val result = mutableMapOf<Material, Int>()

            repeat(picks) {
                if (pool.isEmpty()) return@repeat

                val totalWeight = pool.sumOf { it.third }
                val roll = Random.nextDouble(totalWeight)

                var current = 0.0
                var pickedIndex = -1

                for ((i, triple) in pool.withIndex()) {
                    current += triple.third
                    if (roll <= current) {
                        pickedIndex = i
                        break
                    }
                }

                if (pickedIndex >= 0) {
                    val (material, amount, _) = pool.removeAt(pickedIndex)
                    result[material] = amount
                }
            }

            return result
        }
    }
}