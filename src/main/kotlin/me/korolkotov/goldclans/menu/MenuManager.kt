package me.korolkotov.goldclans.menu

import me.korolkotov.goldclans.Main
import me.korolkotov.goldclans.load.LoadManagerInterface
import me.korolkotov.goldclans.util.TaskService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.scheduler.BukkitRunnable

class MenuManager : LoadManagerInterface<MenuManager>, Listener {
    private val menus = mutableListOf<Menu>()

    override fun getInstance() = this

    override fun initialize() {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)

        TaskService.runTimer("menu-update", 0L, 20L) {
            menus.toList().forEach(Menu::update)
        }
    }

    override fun reload() {
        terminate()
    }

    override fun terminate() {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (player.openInventory.topInventory.holder is Menu)
                player.closeInventory()
        }
    }

    fun addMenu(menu: Menu) {
        menus.add(menu)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clicked = event.clickedInventory ?: return
        val top = event.view.topInventory.holder
        if (top is Menu) {
            if (clicked.holder is Menu) top.onClick(event)
            else top.onClickOutside(event)
            if (!top.canDrag(event.slot)) event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val top = event.view.topInventory.holder
        if (top is Menu) {
            object : BukkitRunnable() {
                override fun run() {
                    top.onClose(event.player as Player)
                    if (event.view.topInventory.viewers.isEmpty()) menus.remove(top)
                }
            }.runTaskLater(Main.instance, 1)
        }
    }
}