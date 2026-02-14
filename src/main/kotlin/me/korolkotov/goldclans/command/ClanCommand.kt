package me.korolkotov.goldclans.command

import kotlinx.coroutines.launch
import me.korolkotov.goldclans.Main
import me.korolkotov.goldclans.annotations.SubCommand
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.clan.model.Clan
import me.korolkotov.goldclans.clan.model.ClanLevelInfo
import me.korolkotov.goldclans.clan.model.ClanRole
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.economy.EconomyManager
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.menu.impls.ClanMenu
import me.korolkotov.goldclans.util.MessageService
import me.korolkotov.goldclans.util.TaskService
import me.korolkotov.goldclans.util.asComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.math.max

class ClanCommand : CommandExecutor() {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    private val invited = mutableMapOf<UUID, List<Clan>>()

    @SubCommand(commands = ["help"], permissionNode = "help")
    fun help(sender: CommandSender) {
        sendHelpMessage(sender)
        if (sender.hasPermission("goldclans.clanchat"))
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.helpConfig.getMessage("clanchat"))
    }

    @SubCommand(commands = ["create"], permissionNode = "create")
    fun create(player: Player, name: String) {
        if (clanManager.getClanByPlayer(player) != null || clanManager.getClanMember(player).role == ClanRole.LEADER) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyInClan)
            return
        }

        val raw = MessageService.raw(name)
        val nameSymbols = ConfigManager.instance.config.clan.nameSymbols
        if (raw.any { !nameSymbols.contains(it) }) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.wrongName)
            return
        }

        val lengthRange = ConfigManager.instance.config.clan.nameLength
        if (raw.length !in lengthRange) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.wrongClanName,
                mapOf("%min%" to lengthRange.min().toString(), "%max%" to lengthRange.max().toString()))
            return
        }

        if (clanManager.getClanByName(raw) != null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.clanAlreadyExists)
            return
        }

        val cost = ConfigManager.instance.config.clan.createCost.toDouble()
        if (!EconomyManager.instance.has(player, cost)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.notEnoughMoney,
                mapOf("%amount%" to EconomyManager.instance.format(cost)))
            return
        }

        EconomyManager.instance.withdraw(player, cost)
        val clan = clanManager.createClan(player, name)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.create,
            mapOf("%name%" to clan.stylizedName()))
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
                mapOf("%name%" to clan.stylizedName()))
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
    fun promote(player: Player, playerName: String) {
        val other = clanManager.getClanMember(playerName)
        if (other == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.playerNotFound)
            return
        }

        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clan.leader != player.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notLeader)
            return
        }

        if (clan.leader == other.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.cantInteractYourself)
            return
        }

        if (!clan.has(other)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInYourClan)
            return
        }

        if (other.role == ClanRole.MODERATOR) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyPromoted)
            return
        }

        other.role = ClanRole.MODERATOR
        PluginCoroutineScope.scope.launch { clanManager.repository.clanMemberDao.upsert(other) }
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.promote,
            mapOf("%player%" to other.name))
        if (other.player != null) MessageService.sendMessage(other.player!!, ConfigManager.instance.messageConfig.commandsConfig.promoted,
            mapOf("%player%" to player.name))
    }

    @SubCommand(commands = ["demote"], permissionNode = "demote")
    fun demote(player: Player, playerName: String) {
        val other = clanManager.getClanMember(playerName)
        if (other == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.playerNotFound)
            return
        }
        
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clan.leader != player.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notLeader)
            return
        }

        if (clan.leader == other.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.cantInteractYourself)
            return
        }
        
        if (!clan.has(other)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInYourClan)
            return
        }

        if (other.role != ClanRole.MODERATOR) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notPromoted)
            return
        }

        other.role = ClanRole.MEMBER
        PluginCoroutineScope.scope.launch { clanManager.repository.clanMemberDao.upsert(other) }
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.promote,
            mapOf("%player%" to other.name))
        if (other.player != null) MessageService.sendMessage(other.player!!, ConfigManager.instance.messageConfig.commandsConfig.promoted,
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

        if (player.uniqueId == other.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.cantInteractYourself)
            return
        }

        val member = clanManager.getClanMember(other)
        if (member.clanId != null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyInOtherClan)
            return
        }

        if (invited.getOrDefault(other.uniqueId, emptyList()).contains(clan)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyInvited)
            return
        }

        invited[other.uniqueId] = invited.getOrDefault(other.uniqueId, emptyList()) + clan
        var responded = false
        val accept = MessageService.format("&a[Принять]").asComponent().clickEvent(ClickEvent.callback {
            if (responded) return@callback
            responded = true
            removeInvite(other, clan)

            if (member.clanId != null) {
                MessageService.sendMessage(other, ConfigManager.instance.messageConfig.clanErrorsConfig.alreadyInClan)
                return@callback
            }

            member.clanId = clan.id
            member.role = ClanRole.MEMBER
            clan.add(member)
            PluginCoroutineScope.scope.launch { clanManager.repository.clanMemberDao.upsert(member) }

            if (player.isOnline) MessageService.sendMessage(player, ConfigManager.instance.messageConfig.inviteConfig.accepted,
                mapOf("%player%" to other.name, "%clan%" to clan.stylizedName()))
            if (other.isOnline) MessageService.sendMessage(other, ConfigManager.instance.messageConfig.inviteConfig.accept,
                mapOf("%player%" to player.name, "%clan%" to clan.stylizedName()))
        })
        val deny = MessageService.format("&c[Отклонить]").asComponent().clickEvent(ClickEvent.callback {
            if (responded) return@callback
            responded = true
            removeInvite(other, clan)

            if (player.isOnline) MessageService.sendMessage(player, ConfigManager.instance.messageConfig.inviteConfig.denied,
                mapOf("%player%" to other.name, "%clan%" to clan.stylizedName()))
            if (other.isOnline) MessageService.sendMessage(other, ConfigManager.instance.messageConfig.inviteConfig.deny,
                mapOf("%player%" to player.name, "%clan%" to clan.stylizedName()))
        })
        TaskService.runLater("invite-cancel", 20L * 30) {
            responded = true
            removeInvite(other, clan)
        }
        val component = accept.append(Component.text(" ")).append(deny)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.invite,
            mapOf("%player%" to other.name, "%clan%" to clan.stylizedName()))
        MessageService.sendMessage(other, ConfigManager.instance.messageConfig.commandsConfig.invited,
            mapOf("%player%" to player.name, "%clan%" to clan.stylizedName()))
        other.sendMessage(component)
    }

    @SubCommand(commands = ["kick"], permissionNode = "kick")
    fun kick(player: Player, playerName: String) {
        val other = clanManager.getClanMember(playerName)
        if (other == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.errorsConfig.playerNotFound)
            return
        }

        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clanManager.getClanMember(player).role == ClanRole.MEMBER) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notForMembers)
            return
        }

        if (player.uniqueId == other.uniqueId) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.cantInteractYourself)
            return
        }

        if (!clan.has(other)) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInYourClan)
            return
        }

        if (other.role != ClanRole.MEMBER) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.cantKick)
            return
        }

        other.clanId = null
        other.role = null
        clan.remove(other)
        PluginCoroutineScope.scope.launch { clanManager.repository.clanMemberDao.upsert(other) }

        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.kick,
            mapOf("%player%" to other.name, "%clan%" to clan.stylizedName()))
        if (other.player != null) MessageService.sendMessage(other.player!!, ConfigManager.instance.messageConfig.commandsConfig.kicked,
            mapOf("%player%" to player.name, "%clan%" to clan.stylizedName()))
    }

    @SubCommand(commands = ["menu"], permissionNode = "menu")
    fun menu(player: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        val menu = ClanMenu(clan)
        menu.open(player)
    }

    @SubCommand(commands = ["leave"], permissionNode = "leave")
    fun leave(player: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        val member = clanManager.getClanMember(player)
        if (member.role == ClanRole.LEADER) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.leaderCantQuit)
            return
        }

        member.clanId = null
        member.role = null
        clan.remove(member)
        PluginCoroutineScope.scope.launch { clanManager.repository.clanMemberDao.upsert(member) }

        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.leave,
            mapOf("%clan%" to clan.stylizedName()))
    }

    @SubCommand(commands = ["sethome"], permissionNode = "sethome")
    fun setHome(player: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        val member = clanManager.getClanMember(player)
        if (member.role == ClanRole.MEMBER) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notForMembers)
            return
        }

        clan.home = player.location.clone()
        PluginCoroutineScope.scope.launch { clanManager.repository.clanDao.update(clan) }
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.setHome)
    }

    @SubCommand(commands = ["delhome"], permissionNode = "delhome")
    fun deleteHome(player: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        val member = clanManager.getClanMember(player)
        if (member.role == ClanRole.MEMBER) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notForMembers)
            return
        }

        clan.home = null
        PluginCoroutineScope.scope.launch { clanManager.repository.clanDao.update(clan) }
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.delHome)
    }

    @SubCommand(commands = ["home"], permissionNode = "home")
    fun home(player: Player) {
        val clan = clanManager.getClanByPlayer(player)
        if (clan == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.notInClan)
            return
        }

        if (clan.home == null) {
            MessageService.sendMessage(player, ConfigManager.instance.messageConfig.clanErrorsConfig.homeIsNull)
            return
        }

        player.teleport(clan.home!!)
        MessageService.sendMessage(player, ConfigManager.instance.messageConfig.commandsConfig.home)
    }

    @SubCommand(commands = ["level"], permissionNode = "level")
    fun level(sender: CommandSender, action: String, clan: String, amount: Int) {
        val levelAction = LevelAction.entries.firstOrNull { it.name.equals(action, true) }
        if (levelAction == null) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.notEnoughArgs,
                mapOf("%usage%" to MessageService.format(ConfigManager.instance.messageConfig.helpConfig.getMessage("level"))))
            return
        }

        val clan = clanManager.getClanByName(MessageService.raw(clan))
        if (clan == null) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.clanErrorsConfig.clanNotFound)
            return
        }

        if (amount < 1) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.wrongAmount)
            return
        }

        when (levelAction) {
            LevelAction.SET -> {
                if (clan.level != amount) {
                    clan.level = amount
                    val newInfo = ClanLevelInfo.getRandom()
                    clan.nextLevelInfo.levelCost = newInfo.levelCost
                    clan.nextLevelInfo.levelResources.clear()
                    clan.nextLevelInfo.levelResources.putAll(newInfo.levelResources)
                    PluginCoroutineScope.scope.launch { clanManager.repository.clanDao.update(clan) }
                }
                MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.commandsConfig.levelSet,
                    mapOf("%amount%" to amount.toString(), "%clan%" to clan.stylizedName(), "%level%" to clan.level.toString()))
            }
            LevelAction.ADD -> {
                clan.level += amount
                val newInfo = ClanLevelInfo.getRandom()
                clan.nextLevelInfo.levelCost = newInfo.levelCost
                clan.nextLevelInfo.levelResources.clear()
                clan.nextLevelInfo.levelResources.putAll(newInfo.levelResources)
                PluginCoroutineScope.scope.launch { clanManager.repository.clanDao.update(clan) }

                MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.commandsConfig.levelAdd,
                    mapOf("%amount%" to amount.toString(), "%clan%" to clan.stylizedName(), "%level%" to clan.level.toString()))
            }
            LevelAction.REMOVE -> {
                clan.level = max(1, clan.level - amount)
                val newInfo = ClanLevelInfo.getRandom()
                clan.nextLevelInfo.levelCost = newInfo.levelCost
                clan.nextLevelInfo.levelResources.clear()
                clan.nextLevelInfo.levelResources.putAll(newInfo.levelResources)
                PluginCoroutineScope.scope.launch { clanManager.repository.clanDao.update(clan) }

                MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.commandsConfig.levelRemove,
                    mapOf("%amount%" to amount.toString(), "%clan%" to clan.stylizedName(), "%level%" to clan.level.toString()))
            }
        }
    }

    @SubCommand(commands = ["delete"], permissionNode = "delete")
    fun delete(sender: CommandSender, clanName: String) {
        val clan = clanManager.getClanByName(MessageService.raw(clanName))
        if (clan == null) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.clanErrorsConfig.clanNotFound)
            return
        }

        clanManager.removeClan(clan)
        MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.commandsConfig.remove,
            mapOf("%name%" to clan.stylizedName()))
    }

    @SubCommand(commands = ["reload"], permissionNode = "reload")
    fun reload(sender: CommandSender) {
        Main.instance.loadManager.reload()
        MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.commandsConfig.reload)
    }

    private fun removeInvite(player: Player, clan: Clan) {
        val list = invited.getOrDefault(player.uniqueId, emptyList()) - clan
        if (list.isEmpty()) invited.remove(player.uniqueId)
        else invited[player.uniqueId] = list
    }

    private enum class LevelAction {
        SET,
        ADD,
        REMOVE
    }
}