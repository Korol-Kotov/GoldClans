package me.korolkotov.goldclans.database.dao

import me.korolkotov.goldclans.clan.model.Clan
import java.util.UUID

interface ClanDao {
    fun create(clan: Clan): Int
    fun findById(id: Int): Clan?
    fun findByName(name: String): Clan?
    fun findByLeader(leader: UUID): Clan?
    fun findTopByLevel(limit: Int): List<Clan>
    fun findAll(): List<Clan>
    fun update(clan: Clan)
    fun delete(id: Int)
}