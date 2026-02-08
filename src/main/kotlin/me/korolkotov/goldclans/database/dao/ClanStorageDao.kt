package me.korolkotov.goldclans.database.dao

import me.korolkotov.goldclans.clan.model.ClanSlot

interface ClanStorageDao {
    fun set(slot: ClanSlot)
    fun find(clanId: String, slot: Int): ClanSlot?
    fun findAll(clanId: String): List<ClanSlot>
    fun clear(clanId: String, slot: Int)
}