package me.korolkotov.goldclans.clan.model

import java.time.Instant
import java.util.UUID

data class ClanMember(
    val uniqueId: UUID,
    val name: String,
    var clanId: Int?,
    var role: ClanRole?,
    var joinedAt: Instant
)