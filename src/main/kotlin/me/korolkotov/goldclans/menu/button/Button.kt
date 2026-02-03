package me.korolkotov.goldclans.menu.button

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface Button {
    fun getSlots(): List<Int>

    fun getItem(slot: Int): ItemStack

    fun onClick(data: ClickData)

    data class ClickData(
        val player: Player,
        val button: Button,
        val inventory: Inventory,
        val slot: Int,
        val clickType: ClickType
    ) {
        companion object {
            fun from(button: Button, event: InventoryClickEvent): ClickData {
                return ClickData(event.whoClicked as Player, button, event.inventory, event.slot, event.click)
            }
        }
    }
}