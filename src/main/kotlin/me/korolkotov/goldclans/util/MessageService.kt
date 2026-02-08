package me.korolkotov.goldclans.util

import me.korolkotov.goldclans.config.ConfigManager
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

object MessageService {
    private val prefix get() = ConfigManager.instance.messageConfig.prefix

    fun format(text: String, replacements: Map<String, String> = emptyMap()): String {
        var result = text
        replacements.forEach { (key, value) -> result = result.replace(key, value) }

        val hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})")
        val matcher = hexPattern.matcher(result)
        val buffer = StringBuffer()

        while (matcher.find()) {
            val hex = matcher.group(1)
            val replacement = StringBuilder("§x")
            hex.toCharArray().forEach { char -> replacement.append('§').append(char) }
            matcher.appendReplacement(buffer, replacement.toString())
        }
        matcher.appendTail(buffer)

        return ChatColor.translateAlternateColorCodes('&', buffer.toString())
    }

    fun format(list: List<String>, replacements: Map<String, String> = emptyMap()): List<String> {
        return list.map { format(it, replacements) }
    }

    fun raw(text: String): String {
        val formatted = format(text)
        val component = LegacyComponentSerializer.legacySection().deserialize(formatted)
        return PlainTextComponentSerializer.plainText().serialize(component)
    }

    fun sendMessage(sender: CommandSender, message: String, replacements: Map<String, String> = emptyMap()) {
        sender.sendMessage(format(prefix + message, replacements))
    }

    fun sendMessage(sender: CommandSender, message: List<String>, replacements: Map<String, String> = emptyMap()) {
        format(listOf(prefix) + message, replacements).forEach(sender::sendMessage)
    }

    fun broadcast(message: String, replacements: Map<String, String> = emptyMap()) {
        Bukkit.getOnlinePlayers().forEach { player -> sendMessage(player, message, replacements) }
    }

    fun broadcast(message: List<String>, replacements: Map<String, String> = emptyMap()) {
        Bukkit.getOnlinePlayers().forEach { player -> sendMessage(player, message, replacements) }
    }
}