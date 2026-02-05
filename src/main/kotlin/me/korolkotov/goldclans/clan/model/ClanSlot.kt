package me.korolkotov.goldclans.clan.model

import org.bukkit.inventory.ItemStack

data class ClanSlot(
    val clanId: String,
    val slot: Int,
    var item: ItemStack
)