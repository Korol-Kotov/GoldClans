package me.korolkotov.goldclans.clan.cache

import me.korolkotov.goldclans.clan.model.ClanMember
import java.util.UUID

class ClanMemberCache : Cache<UUID, ClanMember>() {
    val members get() = cache.values.toList()
}