package me.korolkotov.goldclans.command

import me.korolkotov.goldclans.load.LoadManagerInterface
import me.korolkotov.goldclans.logger.Logger
import org.bukkit.Bukkit

class CommandManager : LoadManagerInterface<CommandManager> {
    override fun getInstance() = this

    override fun initialize() {
        val command = Bukkit.getPluginCommand("goldclan")
        if (command != null) {
            val executor = ClanCommand()

            command.setExecutor(executor)
            command.tabCompleter = executor
            Logger.instance.debug("Command ${command.name} has been registered with executor ${executor::class.simpleName}.")
        }

        val clanChatCommand = Bukkit.getPluginCommand("clanchat")
        if (clanChatCommand != null) {
            val executor = ClanChatCommand()

            clanChatCommand.setExecutor(executor)
            clanChatCommand.tabCompleter = executor
            Logger.instance.debug("Command ${clanChatCommand.name} has been registered with executor ${executor::class.simpleName}.")
        }
    }
}