package me.korolkotov.goldclans.mythicmobs

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent
import me.korolkotov.goldclans.Main
import me.korolkotov.goldclans.load.LoadManagerInterface
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicMobsManager : LoadManagerInterface<MythicMobsManager>, Listener {
    override fun getInstance(): MythicMobsManager = this

    override fun initialize() {
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs"))
            Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onMythicConditionLoad(event: MythicConditionLoadEvent) {
        if (event.conditionName.equals("teamclan", true)) {
            event.register(ClanCondition(event.argument))
        }
    }
}