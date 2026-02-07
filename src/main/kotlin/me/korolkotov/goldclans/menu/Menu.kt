package me.korolkotov.goldclans.menu

import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.config.MenuConfig
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.menu.button.Button
import me.korolkotov.goldclans.util.asComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class Menu(id: String) : InventoryHolder {
    private val buttons = mutableListOf<Button>()
    protected val config: MenuConfig = ConfigManager.instance.getMenus().firstOrNull { it.id.equals(id, true) }
        ?: throw RuntimeException("Can't get menu by id $id")

    protected lateinit var inv: Inventory

    private var initialized = false

    fun init(player: Player) {
        inv = createInventory()

        buttons.clear()
        initButtons(player)

        for (button in buttons) {
            if (button.getSlots().isEmpty()) continue

            for (slot in button.getSlots()) {
                inv.setItem(slot, button.getItem(slot))
            }
        }

        LoadManager.getInstance(MenuManager::class.java).addMenu(this)
    }

    override fun getInventory(): Inventory = inv

    fun update(slot: Int) {
        val button = buttons.firstOrNull { it.getSlots().contains(slot) } ?: return
        inv.setItem(slot, button.getItem(slot))
    }

    fun update() {
        for (i in 0..inv.size) update(i)
    }

    fun onClick(event: InventoryClickEvent) {
        val clicked = inv.getItem(event.slot)
        if (clicked == null || clicked.type.isEmpty) return

        val button = buttons.firstOrNull { button -> button.getSlots().contains(event.slot) } ?: return
        button.onClick(Button.ClickData.from(button, event))
    }

    fun open(player: Player) {
        if (!initialized) {
            init(player)
            initialized = true
        }
        player.openInventory(inv)
    }

    abstract fun canDrag(slot: Int): Boolean
    protected open fun createInventory(): Inventory = Bukkit.createInventory(this, config.size, config.title.asComponent())
    protected abstract fun initButtons(player: Player)

    open fun onClose(player: Player) {}
    open fun onClickOutside(event: InventoryClickEvent) {}

    protected fun getButtons() = buttons.toList()

    protected fun addButton(button: Button) {
        if (buttons.contains(button)) return
        if (button.getSlots().isEmpty()) return

        buttons.add(button)
    }
}