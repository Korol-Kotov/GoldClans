package me.korolkotov.goldclans.clan.model

import org.bukkit.Location
import org.bukkit.entity.Player
import java.time.Instant
import java.util.UUID

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
    private val members = mutableListOf<ClanMember>()
    var isRemoved = false

    fun has(member: ClanMember) = members.any { it.uniqueId == member.uniqueId }
    fun has(player: Player) = members.any { it.uniqueId == player.uniqueId }

    fun add(member: ClanMember) {
        if (members.any { it.uniqueId == member.uniqueId }) return
        members.add(member)
    }

    fun remove(member: ClanMember) {
        members.remove(member)
    }

    fun members() = members.toList()
}