package me.korolkotov.goldclans.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

class GeneralConfig(yaml: YamlConfiguration) {
    val plugin = PluginConfig(yaml.getConfigurationSection("plugin")!!)

    val economy = EconomyConfig(yaml.getConfigurationSection("economy")!!)
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