package me.korolkotov.goldclans.economy

import me.korolkotov.goldclans.Main
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.load.LoadManagerInterface
import me.korolkotov.goldclans.logger.Logger
import me.korolkotov.goldclans.util.format
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class EconomyManager : LoadManagerInterface<EconomyManager> {
    companion object {
        lateinit var instance: EconomyManager private set
    }

    private lateinit var vaultEconomy: VaultEconomy

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        if (!initVault()) {
            Logger.instance.error("Vault dependency wasn't found, disabling plugin.")
            Bukkit.getPluginManager().disablePlugin(Main.instance)
            return
        }
    }

    private fun initVault(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return false
        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
        vaultEconomy = VaultEconomy(rsp.provider)
        return true
    }

    fun has(player: OfflinePlayer, amount: Double): Boolean = vaultEconomy.has(player, amount)

    fun withdraw(player: OfflinePlayer, amount: Double): Boolean = vaultEconomy.withdraw(player, amount)

    fun deposit(player: OfflinePlayer, amount: Double): Boolean = vaultEconomy.deposit(player, amount)

    fun format(amount: Double): String = amount.format() + ConfigManager.instance.config.economy.symbols.vault
}