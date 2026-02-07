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
import me.korolkotov.goldclans.menu.button.CloseButton
import me.korolkotov.goldclans.menu.button.ItemButton
import me.korolkotov.goldclans.menu.button.SimpleButton
import me.korolkotov.goldclans.util.ItemBuilder
import me.korolkotov.goldclans.util.MessageService
import me.korolkotov.goldclans.util.asComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class ClanMenu(
    private val clan: Clan
) : Menu("clan-menu") {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    override fun createInventory(): Inventory {
        val title = MessageService.format(config.title, mapOf("%name%" to clan.stylizedName()))
        return Bukkit.createInventory(this, config.size, title.asComponent())
    }

    override fun canDrag(slot: Int) = false

    override fun initButtons(player: Player) {
        val upgrade = config.getItem("upgrade")
        addButton(SimpleButton(
            {
                val lore = MessageService.format(upgrade.getLore(),
                    mapOf("%level%" to clan.level.toString(), "%cost%" to clan.nextLevelInfo.levelCost.toString())).toMutableList()
                val prototype = lore.removeLast()
                for ((material, amount) in clan.nextLevelInfo.resources) {
                    lore.add(MessageService.format(prototype,
                        mapOf("%type%" to material.name, "%amount%" to amount.toString())))
                }
                ItemBuilder(upgrade.getItem()!!).lore(lore).build()
            },
            upgrade.getSlots()
        ) { data ->
            if (data.clickType.isLeftClick) {
                tryTakeResources(data.player)
                checkNextLevel()
                update()
            } else {
                val balance = EconomyManager.instance.balance(data.player)
                if (balance <= 0) {
                    MessageService.sendMessage(data.player, ConfigManager.instance.messageConfig.errorsConfig.notEnoughMoney,
                        mapOf("%amount%" to "до ${EconomyManager.instance.format(clan.nextLevelInfo.levelCost)}"))
                    return@SimpleButton
                }

                val taken = min(balance, clan.nextLevelInfo.levelCost)
                EconomyManager.instance.withdraw(data.player, taken)
                clan.nextLevelInfo.levelCost -= taken.toInt()
                checkNextLevel()
                update()
            }
        })

        val info = config.getItem("info")
        addButton(SimpleButton(
            {
                val name = MessageService.format(info.getDisplayName()!!, mapOf("%name%" to clan.stylizedName()))
                val leader = clanManager.getClanMember(clan.leader)?.name ?: Bukkit.getOfflinePlayer(clan.leader).name!!
                val lore = MessageService.format(info.getLore(),
                    mapOf(
                        "%leader%" to leader,
                        "%level%" to clan.level.toString(),
                        "%slots%" to clan.storageSlots.toString(),
                        "%members%" to clan.members().size.toString()
                    )
                )
                ItemBuilder(info.getItem()!!).name(name).lore(lore).build()
            },
            info.getSlots(),
            {}
        ))

        val storage = config.getItem("storage")
        addButton(SimpleButton(
            { storage.getItem()!! },
            storage.getSlots()
        ) { data ->
            val menu = StorageMenu(clan, 1)
            menu.open(data.player)
        })

        val close = config.getItem("close")
        addButton(CloseButton(
            close.getItem()!!,
            close.getSlots()
        ))

        val filler = config.getItem("filler")
        if (!filler.section.getBoolean("enabled")) return
        val remainSlots = (0..<inv.size) - getButtons().flatMap { it.getSlots() }.toSet()
        val fillerItem = filler.getItem()!!
        for (slot in remainSlots) addButton(ItemButton(fillerItem.clone(), listOf(slot)))
    }

    private fun checkNextLevel() {
        val info = clan.nextLevelInfo
        if (info.levelCost > 0 || info.resources.isNotEmpty()) return
        val newInfo = ClanLevelInfo.getRandom()
        clan.nextLevelInfo.levelCost = newInfo.levelCost
        clan.nextLevelInfo.resources.clear()
        clan.nextLevelInfo.resources.putAll(newInfo.resources)
        clan.level++
        PluginCoroutineScope.scope.launch { clanManager.repository.clanDao.update(clan) }
    }

    private fun tryTakeResources(player: Player) {
        val resources = clan.nextLevelInfo.resources
        for (i in 0..<player.inventory.size) {
            if (resources.isEmpty()) break
            val item = player.inventory.getItem(i) ?: continue
            if (!resources.containsKey(item.type)) continue
            val need = resources[item.type] ?: 0
            if (need <= 0) continue
            val taken = min(item.amount, need)
            if (taken <= 0) continue
            if (taken >= item.amount) {
                resources[item.type] = need - item.amount
                player.inventory.setItem(i, ItemStack(Material.AIR))
            } else {
                resources.remove(item.type)
                item.amount -= need
            }
        }
    }
}