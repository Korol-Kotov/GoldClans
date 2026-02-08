package me.korolkotov.goldclans.config

import me.korolkotov.goldclans.util.toRange
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

class GeneralConfig(yaml: YamlConfiguration) {
    val plugin = PluginConfig(yaml.getConfigurationSection("plugin")!!)

    val economy = EconomyConfig(yaml.getConfigurationSection("economy")!!)
    val clan = ClanConfig(yaml.getConfigurationSection("clan")!!)
}

class PluginConfig(section: ConfigurationSection) {
    val language = section.getString("language")!!
    val debug = section.getBoolean("debug")
}

class EconomyConfig(section: ConfigurationSection) {
    val symbols = EconomySymbolsConfig(section.getConfigurationSection("symbols")!!)
}

class EconomySymbolsConfig(section: ConfigurationSection) {
    val vault = section.getString("vault")!!
}

class ClanConfig(section: ConfigurationSection) {
    val createCost = section.getInt("create-cost")
    val nameLength = section.getString("name-length")!!.toRange()
    val nameSymbols = section.getString("name-symbols")!!

    val levels = ClanLevelsConfig(section.getConfigurationSection("levels")!!)
    val storage = ClanStorageConfig(section.getConfigurationSection("storage")!!)
}

class ClanLevelsConfig(section: ConfigurationSection) {
    val cost = section.getString("cost")!!.toRange()
    val resources = section.getStringList("resources")
}

class ClanStorageConfig(section: ConfigurationSection) {
    val startSlots = section.getInt("start-slots")
    val cost = section.getString("cost")!!.toRange()
    val slotsPerUpgrade = section.getInt("slots-per-upgrade")
}