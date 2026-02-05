package me.korolkotov.goldclans.config

import org.bukkit.configuration.ConfigurationSection

class MessageConfig(section: ConfigurationSection) {
    val prefix = section.getString("prefix")!!

    val helpConfig = HelpConfig(section.getConfigurationSection("help")!!)
    val errorsConfig = ErrorsConfig(section.getConfigurationSection("errors")!!)
    val clanErrorsConfig = ClanErrorsConfig(section.getConfigurationSection("clan-errors")!!)
    val commandsConfig = CommandsConfig(section.getConfigurationSection("commands")!!)
}

class HelpConfig(private val section: ConfigurationSection) {
    val header = section.getString("header")!!

    fun getMessage(key: String) = section.getString(key)!!
}

class ErrorsConfig(section: ConfigurationSection) {
    val notEnoughPerms = section.getString("not-enough-perms")!!
    val notEnoughArgs = section.getString("not-enough-args")!!
    val onlyForPlayer = section.getString("only-for-player")!!
    val somethingWentWrong = section.getString("something-went-wrong")!!
    val notEnoughMoney = section.getString("not-enough-money")!!
}

class ClanErrorsConfig(section: ConfigurationSection) {
    val notInClan = section.getString("not-in-clan")!!
    val alreadyInClan = section.getString("already-in-clan")!!
    val clanAlreadyExists = section.getString("clan-already-exists")!!
    val notLeader = section.getString("not-leader")!!
}

class CommandsConfig(section: ConfigurationSection) {
    val create = section.getString("create")!!
    val remove = section.getString("remove")!!
}