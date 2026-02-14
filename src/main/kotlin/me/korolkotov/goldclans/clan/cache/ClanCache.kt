package me.korolkotov.goldclans.clan.cache

import me.korolkotov.goldclans.clan.model.Clan

class ClanCache : Cache<Int, Clan>() {
    val clans get() = cache.values.toList()
}