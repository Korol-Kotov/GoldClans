package me.korolkotov.goldclans

import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.logger.Logger
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main private set
    }

    val loadManager = LoadManager()

    override fun onEnable() {
        instance = this
        Material.GRAY_STAINED_GLASS_PANE
        loadManager.initialize()
        logger.info("Plugin $name enabled!")
        Logger.instance.debug("Plugin has enabled.")
    }

    override fun onDisable() {
        Logger.instance.debug("Disabling plugin.")
        loadManager.terminate()
        PluginCoroutineScope.shutdown()
        logger.info("Plugin $name disabled!")
    }
}