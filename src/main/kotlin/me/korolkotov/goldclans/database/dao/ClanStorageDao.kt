package me.korolkotov.goldclans.database.dao

import me.korolkotov.goldclans.clan.model.ClanSlot

interface ClanStorageDao {
    fun set(slot: ClanSlot)
    fun find(clanId: Int, slot: Int): ClanSlot?
    fun findAll(clanId: Int): List<ClanSlot>
    fun clear(clanId: Int, slot: Int)
}