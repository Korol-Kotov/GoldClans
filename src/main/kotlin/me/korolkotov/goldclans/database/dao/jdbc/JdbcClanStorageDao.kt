package me.korolkotov.goldclans.database.dao.jdbc

import me.korolkotov.goldclans.clan.model.ClanSlot
import me.korolkotov.goldclans.database.dao.ClanStorageDao
import me.korolkotov.goldclans.util.ItemSerializer
import java.sql.ResultSet
import javax.sql.DataSource

class JdbcClanStorageDao(
    private val ds: DataSource
) : ClanStorageDao {
    override fun set(slot: ClanSlot) {
        ds.connection.use { con ->
            val sql = """
                INSERT INTO clan_storage (clan_id, slot, item_data)
                VALUES (?, ?, ?)
                ON CONFLICT(clan_id, slot) DO UPDATE SET
                    item_data = excluded.item_data
            """
            con.prepareStatement(sql).use { ps ->
                ps.setString(1, slot.clanId)
                ps.setInt(2, slot.slot)
                ps.setString(3, ItemSerializer.serialize(slot.item))
                ps.executeUpdate()
            }
        }
    }

    override fun find(clanId: String, slot: Int): ClanSlot? =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM clan_storage WHERE clan_id = ? AND slot = ?"
            ).use { ps ->
                ps.setString(1, clanId)
                ps.setInt(2, slot)
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.toSlot() else null
                }
            }
        }

    override fun findAll(clanId: String): List<ClanSlot> =
        ds.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM clan_storage WHERE clan_id = ? ORDER BY slot"
            ).use { ps ->
                ps.setString(1, clanId)
                ps.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toSlot())
                        }
                    }
                }
            }
        }

    override fun clear(clanId: String, slot: Int) {
        ds.connection.use { con ->
            con.prepareStatement(
                "DELETE FROM clan_storage WHERE clan_id = ? AND slot = ?"
            ).use {
                it.setString(1, clanId)
                it.setInt(2, slot)
                it.executeUpdate()
            }
        }
    }

    private fun ResultSet.toSlot() = ClanSlot(
        clanId = getString("clan_id"),
        slot = getInt("slot"),
        item = ItemSerializer.deserialize(getString("item_data") ?: "")
    )
}