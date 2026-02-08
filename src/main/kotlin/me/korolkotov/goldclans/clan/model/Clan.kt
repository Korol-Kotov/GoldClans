package me.korolkotov.goldclans.clan.model

import kotlinx.coroutines.launch
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.util.MessageService
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.*
import kotlin.math.min

data class Clan(
    var id: Int,
    val name: String,
    var leader: UUID,
    var home: Location?,
    var level: Int,
    var storageSlots: Int,
    var nextLevelInfo: ClanLevelInfo,
    val createdAt: Instant
) {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    private val slots = mutableListOf<ClanSlot>()
    private val members = mutableListOf<ClanMember>()
    var isRemoved = false

    fun stylizedName(): String {
        val normal = "abcdefghijklmnopqrstuvwxyz"
        val fancy  = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ"

        val map = normal.zip(fancy).toMap()

        return buildString {
            var skipNext = false

            for (ch in name) {
                if (skipNext) {
                    append(ch)
                    skipNext = false
                    continue
                }

                if (ch == '§' || ch == '&') {
                    append(ch)
                    skipNext = true
                    continue
                }

                val lower = ch.lowercaseChar()
                append(map[lower] ?: ch)
            }
        }
    }

    fun rawName(): String = MessageService.raw(name)

    fun has(member: ClanMember) = members.any { it.uniqueId == member.uniqueId }
    fun has(player: Player) = members.any { it.uniqueId == player.uniqueId }

    fun add(member: ClanMember) {
        if (members.any { it.uniqueId == member.uniqueId }) return
        members.add(member)
    }

    fun remove(member: ClanMember) {
        members.remove(member)
    }

    fun bc(text: String) {
        members.mapNotNull { it.player }.forEach { player ->
            MessageService.sendMessage(player, text)
        }
    }

    fun members() = members.toList()
    fun slots() = slots.toList()

    fun addItem(item: ItemStack) {
        var need = item.amount
        for (i in 0..<storageSlots) {
            val slot = getSlot(i) ?: continue
            if (slot.item.amount >= slot.item.maxStackSize) continue
            if (slot.item.isSimilar(item)) {
                val add = min(slot.item.maxStackSize - slot.item.amount, need)
                slot.item.amount += add
                need -= add
                PluginCoroutineScope.scope.launch { clanManager.repository.clanStorageDao.set(slot) }
            }
            if (need <= 0) return
        }
        while (firstEmpty() != -1) {
            val slot = firstEmpty()
            val clanSlot = getSlot(slot) ?: ClanSlot(rawName(), slot, ItemStack(Material.AIR))
            if (!slots.contains(clanSlot)) slots.add(clanSlot)
            val slotItem = item.clone()
            val add = min(slotItem.maxStackSize, need)
            slotItem.amount = add
            clanSlot.item = slotItem
            need -= add
            PluginCoroutineScope.scope.launch { clanManager.repository.clanStorageDao.set(clanSlot) }
            if (need <= 0) return
        }
    }

    fun addSlot(slot: ClanSlot) {
        if (slots.any { it.slot == slot.slot }) return
        slots.add(slot)
    }

    fun getSlot(slot: Int) = slots.firstOrNull { it.slot == slot }

    fun removeSlot(slot: Int) {
        slots.removeAll { it.slot == slot }
    }

    fun canAdd(item: ItemStack): Boolean {
        if (firstEmpty() != -1) return true
        var need = item.amount
        for (i in 0..<storageSlots) {
            val slot = getSlot(i) ?: continue
            if (slot.item.amount >= slot.item.maxStackSize) continue
            if (slot.item.isSimilar(item)) need -= (slot.item.maxStackSize - slot.item.amount)
            if (need <= 0) return true
        }
        return false
    }

    private fun firstEmpty(): Int {
        for (i in 0..<storageSlots) {
            val slot = getSlot(i)
            if (slot == null || slot.item.type.isEmpty) return i
        }
        return -1
    }
}