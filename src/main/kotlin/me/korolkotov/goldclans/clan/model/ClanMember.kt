package me.korolkotov.goldclans.clan.model

import org.bukkit.Bukkit
import java.time.Instant
import java.util.UUID

data class ClanMember(
    val uniqueId: UUID,
    val name: String,
    var clanId: String?,
    var role: ClanRole?,
    var joinedAt: Instant
) {
    val player get() = Bukkit.getPlayer(uniqueId)
}