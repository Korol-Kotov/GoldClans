package me.korolkotov.goldclans.config

import me.korolkotov.goldclans.util.ItemBuilder
import org.bukkit.configuration.ConfigurationSection
import kotlin.collections.orEmpty

class MenuConfig(
    val id: String,
    section: ConfigurationSection
) {
    val title = section.getString("title")!!
    val size = section.getInt("size")

    val items: List<ItemConfig>

    init {
        val itemConfigs = mutableListOf<ItemConfig>()
        for (item in section.getConfigurationSection("items")?.getKeys(false).orEmpty()) {
            val itemSection = section.getConfigurationSection("items.$item") ?: continue
            val config = ItemConfig(item, itemSection)
            itemConfigs.add(config)
        }
        items = itemConfigs.toList()
    }

    fun getItem(id: String) = items.first { it.id.equals(id, true) }
}

class ItemConfig(
    val id: String,
    val section: ConfigurationSection
) {
    fun getSlots(): List<Int> {
        if (!section.contains("slots")) return emptyList()
        if (section.isList("slots")) return section.getIntegerList("slots")
        if (section.isString("slots")) {
            val slots = section.getString("slots") ?: return emptyList()
            if (slots.toIntOrNull() != null) return listOf(slots.toInt())
            val args = slots.split("..", limit = 2)
            val min = args.getOrNull(0)?.toIntOrNull() ?: return emptyList()
            val max = args.getOrNull(1)?.toIntOrNull() ?: return emptyList()
            if (min > max) return emptyList()
            return (min..max).toList()
        }
        return emptyList()
    }

    fun getItem() = runCatching { ItemBuilder.fromConfig(section) }.getOrNull()

    fun getLore(): List<String> {
        if (!section.contains("lore")) return emptyList()
        if (section.isList("lore")) return section.getStringList("lore")
        return emptyList()
    }

    fun getMenu() = section.getString("menu")
}