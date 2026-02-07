package me.korolkotov.goldclans.menu.button

import org.bukkit.inventory.ItemStack

class CloseButton(
    private val itemStack: ItemStack,
    private val slots: List<Int>
) : Button {
    override fun getSlots() = slots.toList()
    override fun getItem(slot: Int) = itemStack.clone()
    override fun onClick(data: Button.ClickData) = data.player.closeInventory()
}