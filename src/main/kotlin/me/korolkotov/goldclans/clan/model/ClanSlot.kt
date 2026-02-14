package me.korolkotov.goldclans.clan.model

import org.bukkit.inventory.ItemStack

data class ClanSlot(
    val clanId: Int,
    val slot: Int,
    var item: ItemStack
)