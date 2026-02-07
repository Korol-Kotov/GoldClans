package me.korolkotov.goldclans.economy

import org.bukkit.OfflinePlayer

interface Economy {
    fun balance(player: OfflinePlayer): Double

    fun has(player: OfflinePlayer, amount: Double): Boolean

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean

    fun deposit(player: OfflinePlayer, amount: Double): Boolean
}