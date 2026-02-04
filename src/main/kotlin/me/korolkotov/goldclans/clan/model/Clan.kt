package me.korolkotov.goldclans.clan.model

import org.bukkit.Location
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
)