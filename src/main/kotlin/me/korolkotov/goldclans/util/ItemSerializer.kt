package me.korolkotov.goldclans.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.Base64

object ItemSerializer {
    fun serialize(item: ItemStack): String {
        if (item.type.isEmpty) return ""
        val bytes = item.serializeAsBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun deserialize(data: String): ItemStack {
        if (data.isEmpty()) return ItemStack(Material.AIR)
        val bytes = Base64.getDecoder().decode(data)
        return ItemStack.deserializeBytes(bytes)
    }
}