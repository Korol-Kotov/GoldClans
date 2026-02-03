package me.korolkotov.goldclans.menu.button

import org.bukkit.inventory.ItemStack

class SimpleButton(
    private val itemStack: (Int) -> ItemStack,
    private val slots: List<Int>,
    private val click: (Button.ClickData) -> Unit
) : Button {
    override fun getSlots() = slots.toList()
    override fun getItem(slot: Int) = itemStack(slot).clone()
    override fun onClick(data: Button.ClickData) = click(data)
}