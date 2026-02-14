package me.korolkotov.goldclans.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.korolkotov.goldclans.Main
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.util.MessageService
import org.bukkit.entity.Player
import java.util.UUID

class Placeholder : PlaceholderExpansion() {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    override fun getIdentifier(): String = "clan"

    override fun getAuthor(): String = Main.instance.name

    override fun getVersion(): String = "1.0.0"

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        val args = params.split('_')
        if (args.isEmpty()) return null
        if (args[0].equals("tag", true)) {
            if (args.size >= 2) {
                val uuid = UUID.fromString(args[1])
                val member = clanManager.getClanMember(uuid) ?: return ""
                val clan = clanManager.getClanByMember(member) ?: return ""
                return " ${MessageService.format(clan.stylizedName())}"
            }

            if (player == null) return ""
            val clan = clanManager.getClanByPlayer(player) ?: return ""
            return " ${MessageService.format(clan.stylizedName())}"
        } else if (args[0].equals("level", true)) {
            if (player == null) return ""
            val clan = clanManager.getClanByPlayer(player) ?: return ""
            return clan.level.toString()
        } else if (args[0].equals("top", true)) {
            if (args.size < 3) return null
            val num = args[1].toIntOrNull() ?: return null
            val clan = clanManager.getClanInTop(num) ?: return ""
            return if (args[2].equals("tag", true)) " ${MessageService.format(clan.stylizedName())}"
            else if (args[2].equals("level", true)) clan.level.toString()
            else ""
        }
        return null
    }
}