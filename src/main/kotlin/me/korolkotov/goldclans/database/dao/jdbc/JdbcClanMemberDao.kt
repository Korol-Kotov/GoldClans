package me.korolkotov.goldclans.database.dao.jdbc

import me.korolkotov.goldclans.clan.model.ClanMember
import me.korolkotov.goldclans.clan.model.ClanRole
import me.korolkotov.goldclans.database.dao.ClanMemberDao
import me.korolkotov.goldclans.util.getInstant
import me.korolkotov.goldclans.util.setInstant
import java.sql.ResultSet
import java.util.UUID
import javax.sql.DataSource

class JdbcClanMemberDao(
    private val ds: DataSource
) : ClanMemberDao {
    override fun upsert(member: ClanMember) {
        ds.connection.use { con ->
            val sql = """
                INSERT INTO clan_members
                (player_uuid, player_name, clan_id, role, joined_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    player_name = excluded.player_name,
                    clan_id = excluded.clan_id,
                    role = excluded.role
            """
            con.prepareStatement(sql).use { ps ->
                var i = 1
                ps.setString(i++, member.uniqueId.toString())
                ps.setString(i++, member.name)
                ps.setInt(i++, member.clanId ?: -1)
                ps.setInt(i, member.role?.id ?: -1)
                ps.setInstant(++i, member.joinedAt)
                ps.executeUpdate()
            }
        }
    }

    override fun findByUuid(uuid: UUID): ClanMember? =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM clan_members WHERE player_uuid = ?"
            ).use { ps ->
                ps.setString(1, uuid.toString())
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.toMember() else null
                }
            }
        }

    override fun findByClan(clanId: Int): List<ClanMember> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM clan_members WHERE clan_id = ?"
            ).use { ps ->
                ps.setInt(1, clanId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toMember())
                        }
                    }
                }
            }
        }

    override fun findAll(): List<ClanMember> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM clan_members ORDER BY joined_at"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toMember())
                        }
                    }
                }
            }
        }

    override fun delete(uuid: UUID) {
        ds.connection.use { con ->
            con.prepareStatement(
                "DELETE FROM clan_members WHERE player_uuid = ?"
            ).use {
                it.setString(1, uuid.toString())
                it.executeUpdate()
            }
        }
    }

    private fun ResultSet.toMember() = ClanMember(
        uniqueId = UUID.fromString(getString("player_uuid")),
        name = getString("player_name"),
        clanId = getInt("clan_id").let { if (it == -1) null else it },
        role = getInt("role").let { if (it == -1) null else ClanRole.entries.first { r -> r.id == it } },
        joinedAt = getInstant("joined_at")
    )
}