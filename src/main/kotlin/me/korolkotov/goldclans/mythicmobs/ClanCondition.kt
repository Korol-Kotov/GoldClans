package me.korolkotov.goldclans.mythicmobs

import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import me.korolkotov.goldclans.clan.ClanManager
import me.korolkotov.goldclans.load.LoadManager
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class ClanCondition(
    line: String
) : ISkillMetaCondition {
    val clanManager get() = LoadManager.getInstance(ClanManager::class.java)

    private val expected: Boolean = line.toBooleanStrictOrNull() ?: true

    override fun check(meta: SkillMetadata): Boolean {
        val casterEntity = BukkitAdapter.adapt(meta.caster.entity)
        val targetEntity: Entity? = BukkitAdapter.adapt(meta.entityTargets.firstOrNull())

        if (casterEntity !is Player) return false
        if (targetEntity !is Player) return false

        val sameClan = areInSameClan(casterEntity, targetEntity)
        return sameClan == expected
    }

    private fun areInSameClan(p1: Player, p2: Player): Boolean {
        val clan1 = clanManager.getClanByPlayer(p1) ?: return false
        val clan2 = clanManager.getClanByPlayer(p2) ?: return false
        return clan1.rawName().equals(clan2.rawName(), true)
    }
}