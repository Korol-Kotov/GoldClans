package me.korolkotov.goldclans.economy

import me.korolkotov.goldclans.logger.Logger
import org.bukkit.OfflinePlayer

class VaultEconomy(
    private val economy: net.milkbowl.vault.economy.Economy
) : Economy {
    override fun has(player: OfflinePlayer, amount: Double): Boolean =
        economy.has(player, amount)

    override fun withdraw(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = economy.withdrawPlayer(player, amount)
        Logger.instance.debug("[Vault] $amount was withdrawn from ${player.name}'s account (transaction success: ${response.transactionSuccess()})")
        return response.transactionSuccess()
    }

    override fun deposit(player: OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0) return true

        val response = economy.depositPlayer(player, amount)
        Logger.instance.debug("[Vault] $amount was deposited to ${player.name}'s account (transaction success: ${response.transactionSuccess()})")
        return response.transactionSuccess()
    }
}