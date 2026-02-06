package me.korolkotov.goldclans.config

import org.bukkit.configuration.ConfigurationSection

class MessageConfig(section: ConfigurationSection) {
    val prefix = section.getString("prefix")!!

    val helpConfig = HelpConfig(section.getConfigurationSection("help")!!)
    val errorsConfig = ErrorsConfig(section.getConfigurationSection("errors")!!)
    val clanErrorsConfig = ClanErrorsConfig(section.getConfigurationSection("clan-errors")!!)
    val warningsConfig = WarningsConfig(section.getConfigurationSection("warnings")!!)
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
    val notInYourClan = section.getString("not-in-your-clan")!!
    val alreadyInOtherClan = section.getString("already-in-other-clan")!!
    val alreadyPromoted = section.getString("already-promoted")!!
    val notPromoted = section.getString("not-promoted")!!
    val notForMembers = section.getString("not-for-members")!!
}

class WarningsConfig(section: ConfigurationSection) {
    val removingClan = section.getString("removing-clan")!!
    val denyRemoving = section.getString("deny-removing")!!
}

class CommandsConfig(section: ConfigurationSection) {
    val create = section.getString("create")!!
    val remove = section.getString("remove")!!
    val promote = section.getString("promote")!!
    val promoted = section.getString("promoted")!!
    val invite = section.getString("invite")!!
    val invited = section.getString("invited")!!
}