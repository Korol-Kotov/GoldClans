package me.korolkotov.goldclans.command

import me.korolkotov.goldclans.annotations.SubCommand
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.economy.EconomyManager
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.util.MessageService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClanCommand : CommandExecutor() {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    @SubCommand(commands = ["help"], permissionNode = "help")
    fun help(sender: CommandSender) {
        sendHelpMessage(sender)
    }

    @SubCommand(commands = ["create"], permissionNode = "create")
    fun create(player: Player, name: String) {
        if (clanManager.getClanByPlayer(player) != null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyInClan)
            return
        }

        val raw = MessageService.raw(name)
        println("Name: $name, raw: $raw")
        if (clanManager.getClanByName(raw) != null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.clanAlreadyExists)
            return
        }

        val cost = ConfigManager.instance.config.clan.createCost.toDouble()
        if (!EconomyManager.instance.has(player, cost)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.notEnoughMoney,
                mapOf("%amount%" to EconomyManager.instance.format(cost)))
        }

        EconomyManager.instance.withdraw(player, cost)
        val clan = clanManager.createClan(player, name)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.create,
            mapOf("%name%" to clan.name))
    }

    @SubCommand(commands = ["remove"], permissionNode = "remove")
    fun remove(player: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clan.leader != player.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notLeader)
            return
        }

        TODO("maybe kinda asking about wanting to remove")

        clanManager.removeClan(clan)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.remove,
            mapOf("%name%" to clan.name))
    }
}