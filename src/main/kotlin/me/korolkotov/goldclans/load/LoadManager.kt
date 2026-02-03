package me.korolkotov.goldclans.load

import me.korolkotov.goldclans.command.CommandManager
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.database.DatabaseManager
import me.korolkotov.goldclans.economy.EconomyManager
import me.korolkotov.goldclans.logger.LoggerManager
import me.korolkotov.goldclans.menu.MenuManager

class LoadManager {
    companion object {
        private val loadManagerInterfaces = mutableListOf<LoadManagerInterface<*>>()

        fun <T> getInstance(clazz: Class<T>): T {
            return loadManagerInterfaces.filterIsInstance(clazz).firstNotNullOfOrNull { (it as LoadManagerInterface<T>).getInstance() }!!
        }
    }

    init {
        loadManagerInterfaces.add(ConfigManager())
        loadManagerInterfaces.add(LoggerManager())
        loadManagerInterfaces.add(DatabaseManager())
        loadManagerInterfaces.add(EconomyManager())
        loadManagerInterfaces.add(MenuManager())
        loadManagerInterfaces.add(CommandManager())
    }

    fun initialize() = loadManagerInterfaces.forEach { it.initialize() }

    fun terminate() = loadManagerInterfaces.reversed().forEach { it.terminate() }

    fun reload() = loadManagerInterfaces.reversed().forEach { it.reload() }
}