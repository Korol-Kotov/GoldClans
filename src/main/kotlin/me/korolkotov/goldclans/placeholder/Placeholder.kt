package me.korolkotov.goldclans.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.korolkotov.goldclans.Main
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.util.MessageService
import org.bukkit.entity.Player

class Placeholder : PlaceholderExpansion() {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    override fun getIdentifier(): String = "clan"

    override fun getAuthor(): String = Main.instance.name

    override fun getVersion(): String = "1.0.0"

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        val args = params.split('_')
        if (args.isEmpty()) return null
        if (args[0].equals("tag", true)) {
            if (player == null) return null
            val clan = clanManager.getClanByPlayer(player) ?: return null
            return MessageService.format(clan.stylizedName())
        } else if (args[0].equals("level", true)) {
            if (player == null) return null
            val clan = clanManager.getClanByPlayer(player) ?: return null
            return clan.level.toString()
        } else if (args[0].equals("top", true)) {
            if (args.size < 3) return null
            val num = args[1].toIntOrNull() ?: return null
            val clan = clanManager.getClanInTop(num) ?: return null
            return if (args[2].equals("tag", true)) clan.stylizedName()
            else if (args[2].equals("level", true)) clan.level.toString()
            else null
        }
        return null
    }
}