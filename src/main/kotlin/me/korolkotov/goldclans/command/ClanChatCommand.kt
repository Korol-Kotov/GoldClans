package me.korolkotov.goldclans.command

import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.util.MessageService
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class ClanChatCommand : TabExecutor {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.onlyForPlayer)
            return true
        }

        if (!sender.hasPermission("goldclans.clanchat")) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.notEnoughPerms)
            return true
        }

        val clan = clanManager.getClanByPlayer(sender)
        if (clan == null) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return true
        }

        if (args.isEmpty()) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.notEnoughArgs,
                mapOf("%usage%" to "/$label текст"))
            return true
        }

        var builder = StringBuilder()
        for (str in args) builder = builder.append(str).append(' ')
        val message = builder.toString().trim()
        clan.bc(MessageService.format(ConfigManager.instance.messageConfig.clanChat,
            mapOf("%player%" to sender.name, "%message%" to message)))

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        return emptyList()
    }
}