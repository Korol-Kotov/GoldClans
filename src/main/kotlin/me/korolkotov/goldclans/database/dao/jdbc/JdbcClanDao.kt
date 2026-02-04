package me.korolkotov.goldclans.database.dao.jdbc

import me.korolkotov.goldclans.clan.model.Clan
import me.korolkotov.goldclans.clan.model.ClanLevelInfo
import me.korolkotov.goldclans.database.dao.ClanDao
import me.korolkotov.goldclans.util.LocationSerializer
import me.korolkotov.goldclans.util.getInstant
import me.korolkotov.goldclans.util.setInstant
import java.sql.ResultSet
import java.sql.Statement
import java.util.UUID
import javax.sql.DataSource

class JdbcClanDao(
    private val ds: DataSource
) : ClanDao {
    override fun create(clan: Clan): Int =
        ds.connection.use { con ->
            val sql = """
                INSERT INTO clans
                (name, leader_uuid, home_location, level, storage_slots, next_level_info, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """
            con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { ps ->
                var i = 1
                ps.setString(i++, clan.name)
                ps.setString(i++, clan.leader.toString())
                ps.setString(i++, LocationSerializer.serialize(clan.home))
                ps.setInt(i++, clan.level)
                ps.setInt(i++, clan.storageSlots)
                ps.setString(i++, ClanLevelInfo.serialize(clan.nextLevelInfo))
                ps.setInstant(i, clan.createdAt)
                ps.executeUpdate()

                ps.generatedKeys.use {
                    it.next()
                    it.getInt(1)
                }
            }
        }

    override fun findById(id: Int): Clan? =
        findOne("SELECT * FROM clans WHERE id = ?", id)

    override fun findByName(name: String): Clan? =
        findOne("SELECT * FROM clans WHERE name = ?", name)

    override fun findByLeader(leader: UUID): Clan? =
        findOne("SELECT * FROM clans WHERE leader_uuid = ?", leader.toString())

    override fun findTopByLevel(limit: Int): List<Clan> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM clans ORDER BY level DESC LIMIT ?"
            ).use { ps ->
                ps.setInt(1, limit)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toClan())
                        }
                    }
                }
            }
        }

    override fun findAll(): List<Clan> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM clans ORDER BY id"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toClan())
                        }
                    }
                }
            }
        }

    override fun update(clan: Clan) {
        ds.connection.use { con ->
            val sql = """
                UPDATE clans SET
                    leader_uuid = ?,
                    home_location = ?,
                    level = ?,
                    storage_slots = ?,
                    next_level_info = ?
                WHERE id = ?
            """
            con.prepareStatement(sql).use { ps ->
                var i = 1
                ps.setString(i++, clan.leader.toString())
                ps.setString(i++, LocationSerializer.serialize(clan.home))
                ps.setInt(i++, clan.level)
                ps.setInt(i++, clan.storageSlots)
                ps.setString(i++, ClanLevelInfo.serialize(clan.nextLevelInfo))
                ps.setInt(i, clan.id)
                ps.executeUpdate()
            }
        }
    }

    override fun delete(id: Int) {
        ds.connection.use { con ->
            con.prepareStatement("DELETE FROM clans WHERE id = ?").use {
                it.setInt(1, id)
                it.executeUpdate()
            }
        }
    }

    private fun findOne(sql: String, param: Any): Clan? =
        ds.connection.use { con ->
            con.prepareStatement(sql).use { ps ->
                when (param) {
                    is Int -> ps.setInt(1, param)
                    is String -> ps.setString(1, param)
                }
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.toClan() else null
                }
            }
        }

    private fun ResultSet.toClan() = Clan(
        id = getInt("id"),
        name = getString("name"),
        leader = UUID.fromString(getString("leader_uuid")),
        home = LocationSerializer.deserialize(getString("home_location")),
        level = getInt("level"),
        storageSlots = getInt("storage_slots"),
        nextLevelInfo = ClanLevelInfo.deserialize(getString("next_level_info")),
        createdAt = getInstant("created_at")
    )
}