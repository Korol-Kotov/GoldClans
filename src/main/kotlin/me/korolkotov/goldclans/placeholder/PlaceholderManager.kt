package me.korolkotov.goldclans.placeholder

import me.korolkotov.goldclans.load.LoadManagerInterface
import org.bukkit.Bukkit

class PlaceholderManager : LoadManagerInterface<PlaceholderManager> {
    override fun getInstance(): PlaceholderManager = this

    override fun initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return
        val placeholder = Placeholder()
        if (placeholder.canRegister()) placeholder.register()
    }
}