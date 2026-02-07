package me.korolkotov.goldclans.config

import org.bukkit.configuration.ConfigurationSection

class MessageConfig(section: ConfigurationSection) {
    val prefix = section.getString("prefix")!!
    val clanChat = section.getString("clan-chat")!!

    val helpConfig = HelpConfig(section.getConfigurationSection("help")!!)
    val errorsConfig = ErrorsConfig(section.getConfigurationSection("errors")!!)
    val clanErrorsConfig = ClanErrorsConfig(section.getConfigurationSection("clan-errors")!!)
    val warningsConfig = WarningsConfig(section.getConfigurationSection("warnings")!!)
    val inviteConfig = InviteConfig(section.getConfigurationSection("invite")!!)
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
    val playerNotFound = section.getString("player-not-found")!!
    val wrongAmount = section.getString("wrong-amount")!!
    val cleanInventory = section.getString("clean-inventory")!!
}

class ClanErrorsConfig(section: ConfigurationSection) {
    val wrongClanName = section.getString("wrong-clan-name")!!
    val notInClan = section.getString("not-in-clan")!!
    val alreadyInClan = section.getString("already-in-clan")!!
    val clanAlreadyExists = section.getString("clan-already-exists")!!
    val clanNotFound = section.getString("clan-not-found")!!
    val notLeader = section.getString("not-leader")!!
    val notInYourClan = section.getString("not-in-your-clan")!!
    val cantInteractYourself = section.getString("cant-interact-yourself")!!
    val alreadyInOtherClan = section.getString("already-in-other-clan")!!
    val alreadyPromoted = section.getString("already-promoted")!!
    val notPromoted = section.getString("not-promoted")!!
    val notForMembers = section.getString("not-for-members")!!
    val alreadyInvited = section.getString("already-invited")!!
    val cantKick = section.getString("cant-kick")!!
    val leaderCantQuit = section.getString("leader-cant-quit")!!
    val homeIsNull = section.getString("home-is-null")!!
}

class WarningsConfig(section: ConfigurationSection) {
    val removingClan = section.getString("removing-clan")!!
    val denyRemoving = section.getString("deny-removing")!!
}

class InviteConfig(section: ConfigurationSection) {
    val deny = section.getString("deny")!!
    val denied = section.getString("denied")!!
    val accept = section.getString("accept")!!
    val accepted = section.getString("accepted")!!
}

class CommandsConfig(section: ConfigurationSection) {
    val create = section.getString("create")!!
    val remove = section.getString("remove")!!
    val promote = section.getString("promote")!!
    val promoted = section.getString("promoted")!!
    val invite = section.getString("invite")!!
    val invited = section.getString("invited")!!
    val kick = section.getString("kick")!!
    val kicked = section.getString("kicked")!!
    val leave = section.getString("leave")!!
    val setHome = section.getString("set-home")!!
    val delHome = section.getString("del-home")!!
    val home = section.getString("home")!!
    val levelSet = section.getString("level-set")!!
    val levelAdd = section.getString("level-add")!!
    val levelRemove = section.getString("level-remove")!!
    val reload = section.getString("reload")!!
}