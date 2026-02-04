package me.korolkotov.goldclans.database.dao

import me.korolkotov.goldclans.clan.model.ClanMember
import java.util.UUID

interface ClanMemberDao {
    fun upsert(member: ClanMember)
    fun findByUuid(uuid: UUID): ClanMember?
    fun findByClan(clanId: Int): List<ClanMember>
    fun findAll(): List<ClanMember>
    fun delete(uuid: UUID)
}