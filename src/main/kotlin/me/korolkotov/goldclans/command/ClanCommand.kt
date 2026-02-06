package me.korolkotov.goldclans.command

import kotlinx.coroutines.launch
import me.korolkotov.goldclans.annotations.SubCommand
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.clan.model.Clan
import me.korolkotov.goldclans.clan.model.ClanRole
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.economy.EconomyManager
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.util.MessageService
import me.korolkotov.goldclans.util.asComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class ClanCommand : CommandExecutor() {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    private val invited = mutableMapOf<UUID, List<Clan>>()

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

        var responded = false
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.warningsConfig.removingClan)
        val accept = MessageService.format("&a[Да]").asComponent().clickEvent(ClickEvent.callback {
            if (responded) return@callback
            responded = true

            val clan = clanManager.getClanByPlayer(player)
            if (clan == null) {
                MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
                return@callback
            }

            if (clan.leader != player.uniqueId) {
                MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notLeader)
                return@callback
            }

            clanManager.removeClan(clan)
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.remove,
                mapOf("%name%" to clan.name))
        })
        val deny = MessageService.format("&c[Нет]").asComponent().clickEvent(ClickEvent.callback {
            if (responded) return@callback
            responded = true

            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.warningsConfig.denyRemoving)
        })
        val component = accept.append(Component.text(" ")).append(deny)
        player.sendMessage(component)
    }

    @SubCommand(commands = ["promote"], permissionNode = "promote")
    fun promote(player: Player, other: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clan.leader != player.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notLeader)
            return
        }

        val member = clanManager.getClanMember(other)
        if (!clan.has(member)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInYourClan)
            return
        }

        if (member.role == ClanRole.MODERATOR) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyPromoted)
            return
        }

        member.role = ClanRole.MODERATOR
        PluginCoroutineScope.scope.launch { clanManager.repository.clanMemberDao.upsert(member) }
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.promote,
            mapOf("%player%" to other.name))
        MessageService.sendMessage(other, ConfigManager.instance.messageConfig.commandsConfig.promoted,
            mapOf("%player%" to player.name))
    }

    @SubCommand(commands = ["demote"], permissionNode = "demote")
    fun demote(player: Player, other: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clan.leader != player.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notLeader)
            return
        }

        val member = clanManager.getClanMember(other)
        if (!clan.has(member)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInYourClan)
            return
        }

        if (member.role != ClanRole.MODERATOR) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notPromoted)
            return
        }

        member.role = ClanRole.MEMBER
        PluginCoroutineScope.scope.launch { clanManager.repository.clanMemberDao.upsert(member) }
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.promote,
            mapOf("%player%" to other.name))
        MessageService.sendMessage(other, ConfigManager.instance.messageConfig.commandsConfig.promoted,
            mapOf("%player%" to player.name))
    }

    @SubCommand(commands = ["invite"], permissionNode = "invite")
    fun invite(player: Player, other: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clanManager.getClanMember(player).role == ClanRole.MEMBER) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notForMembers)
            return
        }

        val member = clanManager.getClanMember(other)
        if (member.clanId != null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyInOtherClan)
            return
        }

        if (invited.getOrDefault(other.uniqueId, emptyList()).contains(clan)) {
            MessageService.sendMessage(player, "")
            return
        }

        invited[other.uniqueId] = invited.getOrDefault(other.uniqueId, emptyList()) + clan
        var responded = false
        val accept = MessageService.format("&a[Принять]").asComponent().clickEvent(ClickEvent.callback {
            if (responded) return@callback
            responded = true
        })
        val deny = MessageService.format("&c[Отклонить]").asComponent().clickEvent(ClickEvent.callback {
            if (responded) return@callback
            responded = true
        })
        val component = accept.append(Component.text(" ")).append(deny)
        other.sendMessage(component)
    }
}