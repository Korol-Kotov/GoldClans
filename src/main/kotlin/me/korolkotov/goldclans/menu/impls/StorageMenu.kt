package me.korolkotov.goldclans.menu.impls

import kotlinx.coroutines.launch
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.clan.model.Clan
import me.korolkotov.goldclans.clan.model.ClanLevelInfo
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.economy.EconomyManager
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.menu.Menu
import me.korolkotov.goldclans.menu.button.SimpleButton
import me.korolkotov.goldclans.util.ItemBuilder
import me.korolkotov.goldclans.util.MessageService
import me.korolkotov.goldclans.util.asComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class StorageMenu(
    private val clan: Clan,
    private val page: Int
) : Menu("storage-menu") {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    override fun createInventory(): Inventory {
        val title = MessageService.format(config.title, mapOf("%name%" to clan.stylizedName()))
        return Bukkit.createInventory(this, config.size, title.asComponent())
    }

    override fun canDrag(slot: Int) = false

    override fun initButtons(player: Player) {
        val slotItem = config.getItem("slot")
        val slotFiller = config.getItem("slot-filler")
        addButton(SimpleButton(
            { slot ->
                val index = slotItem.getSlots().indexOf(slot) + (page - 1) * slotItem.getSlots().size
                if (index >= clan.storageSlots) return@SimpleButton slotFiller.getItem()!!
                val entry = clan.getSlot(index) ?: return@SimpleButton ItemStack(Material.AIR)
                val lore = (entry.item.lore() ?: emptyList()).mapNotNull { LegacyComponentSerializer.legacySection().serialize(it) }
                ItemBuilder(entry.item.clone()).lore(lore + slotItem.getLore()).build()
            },
            slotItem.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton
            val index = slotItem.getSlots().indexOf(data.slot) + (page - 1) * slotItem.getSlots().size
            val entry = clan.getSlot(index) ?: return@SimpleButton
            if (entry.item.type.isEmpty) return@SimpleButton
            val item = entry.item.clone()
            item.amount = if (data.clickType.isLeftClick) item.amount else item.amount / 2
            if (!data.player.canGive(item)) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.cleanInventory)
                return@SimpleButton
            }
            if (item.amount == entry.item.amount) {
                clan.removeSlot(entry.slot)
                PluginCoroutineScope.scope.launch { clanManager.repository.clanStorageDao.clear(clan.rawName(), entry.slot) }
            } else entry.item.amount -= item.amount
            data.player.inventory.addItem(item)
            update()
        })

        val upgrade = config.getItem("upgrade")
        addButton(SimpleButton(
            {
                val lore = MessageService.format(upgrade.getLore(), mapOf("%slots%" to clan.storageSlots.toString()))
                ItemBuilder(upgrade.getItem()!!).lore(lore).build()
            },
            upgrade.getSlots()
        ) { data ->
            if (!EconomyManager.instance.has(data.player, clan.nextLevelInfo.slotCost)) {
                MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.notEnoughMoney,
                    mapOf("%amount%" to EconomyManager.instance.format(clan.nextLevelInfo.slotCost)))
                return@SimpleButton
            }

            EconomyManager.instance.withdraw(data.player, clan.nextLevelInfo.slotCost)
            clan.storageSlots += ConfigManager.instance.config.clan.storage.slotsPerUpgrade
            clan.nextLevelInfo.slotCost = ClanLevelInfo.getRandom().slotCost
            PluginCoroutineScope.scope.launch { clanManager.repository.clanDao.update(clan) }
            update()
        })

        val previousPage = config.getItem("previous-page")
        addButton(SimpleButton(
            { if (page > 1) previousPage.getItem()!! else ItemStack(Material.AIR) },
            previousPage.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton

            val menu = StorageMenu(clan, page - 1)
            menu.open(data.player)
        })

        val nextPage = config.getItem("next-page")
        addButton(SimpleButton(
            {
                val slotItem = config.getItem("slot")
                if (clan.storageSlots > slotItem.getSlots().size * page) nextPage.getItem()!!
                else ItemStack(Material.AIR)
            },
            nextPage.getSlots()
        ) { data ->
            if (inv.getItem(data.slot)?.type?.isEmpty != false) return@SimpleButton

            val menu = StorageMenu(clan, page + 1)
            menu.open(data.player)
        })

        val back = config.getItem("back")
        addButton(SimpleButton(
            { back.getItem()!! },
            back.getSlots()
        ) { data ->
            val menu = ClanMenu(clan)
            menu.open(data.player)
        })
    }

    override fun onClickOutside(event: InventoryClickEvent) {
        val inv = event.clickedInventory ?: return
        if (inv !is PlayerInventory) return
        val item = event.currentItem?.clone() ?: return
        if (item.type.isEmpty) return

        item.amount = if (event.click.isLeftClick) item.amount else item.amount / 2
        if (item.amount <= 0 || item.type.isEmpty) return

        if (!clan.canAdd(item)) return

        if (item.amount >= event.currentItem!!.amount) {
            event.whoClicked.inventory.setItem(event.slot, ItemStack(Material.AIR))
        } else {
            val newItem = item.clone()
            newItem.amount = event.currentItem!!.amount - item.amount
            event.whoClicked.inventory.setItem(event.slot, newItem)
        }
        clan.addItem(item.clone())
        update()
    }

    private fun Player.canGive(itemStack: ItemStack): Boolean {
        var need = itemStack.amount
        for (item in inventory.contents) {
            if (item == null) need -= itemStack.maxStackSize
            else if (item.isSimilar(itemStack)) need -= (item.maxStackSize - item.amount)
            if (need <= 0) return true
        }
        return false
    }
}