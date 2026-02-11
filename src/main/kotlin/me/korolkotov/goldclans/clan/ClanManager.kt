package me.korolkotov.goldclans.clan

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.korolkotov.goldclans.clan.cache.ClanCache
import me.korolkotov.goldclans.clan.cache.ClanMemberCache
import me.korolkotov.goldclans.clan.model.Clan
import me.korolkotov.goldclans.clan.model.ClanLevelInfo
import me.korolkotov.goldclans.clan.model.ClanMember
import me.korolkotov.goldclans.clan.model.ClanRole
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.coroutine.BukkitDispatcher
import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.database.DatabaseManager
import me.korolkotov.goldclans.database.repository.ClanRepository
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.load.LoadManagerInterface
import me.korolkotov.goldclans.logger.Logger
import me.korolkotov.goldclans.util.MessageService
import me.korolkotov.goldclans.util.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.random.Random

class ClanManager : LoadManagerInterface<ClanManager> {
    companion object {
        lateinit var instance: ClanManager private set
    }

    val clanCache = ClanCache()
    val clanMemberCache = ClanMemberCache()

    lateinit var repository: ClanRepository private set

    override fun getInstance() = this

    override fun initialize() {
        repository = LoadManager.getInstance(DatabaseManager::class.java).clanRepository

        PluginCoroutineScope.scope.launch {
            repository.clanDao.findAll().forEach { clan ->
                clanCache.put(clan.rawName(), clan)
            }
            withContext(BukkitDispatcher.MAIN) {
                Logger.instance.debug("Clans have been loaded to the cache")
            }

            repository.clanMemberDao.findAll().forEach { member ->
                clanMemberCache.put(member.uniqueId, member)
                if (member.clanId != null) {
                    val clan = clanCache.get(member.clanId!!) ?: return@forEach
                    clan.add(member)
                }
            }
            withContext(BukkitDispatcher.MAIN) {
                Logger.instance.debug("Clans members have been loaded to the cache")
            }

            clanCache.clans.forEach { clan ->
                repository.clanStorageDao.findAll(clan.rawName()).forEach { slot ->
                    clan.addSlot(slot)
                }
            }
        }
    }

    fun createClan(leader: Player, name: String): Clan {
        val clanName = if (name == MessageService.raw(name)) "&${randomHexColor()}$name" else name
        val member = getClanMember(leader)
        val clan = Clan(
            0,
            MessageService.format(clanName),
            member.uniqueId,
            null,
            1,
            ConfigManager.instance.config.clan.storage.startSlots,
            ClanLevelInfo.getRandom(),
            TimeUtil.now()
        )
        member.clanId = clan.rawName()
        member.role = ClanRole.LEADER
        PluginCoroutineScope.scope.launch { repository.clanMemberDao.upsert(member) }
        clan.add(member)
        clanCache.put(clan.rawName(), clan)
        Logger.instance.debug("Created a clan. (name: ${clan.name} (${clan.rawName()}), leader: ${member.uniqueId})")
        PluginCoroutineScope.scope.launch {
            val id = repository.clanDao.create(clan)
            clan.id = id
        }
        return clan
    }

    fun removeClan(clan: Clan) {
        clan.isRemoved = true
        clan.members().forEach { member ->
            member.clanId = null
            member.role = null
            PluginCoroutineScope.scope.launch { repository.clanMemberDao.upsert(member) }
        }
        clan.slots().forEach { slot ->
            PluginCoroutineScope.scope.launch { repository.clanStorageDao.clear(clan.rawName(), slot.slot) }
        }
        clanCache.remove(clan.rawName())
        PluginCoroutineScope.scope.launch { repository.clanDao.delete(clan.id) }
    }

    fun getClanMember(uniqueId: UUID) = clanMemberCache.get(uniqueId)

    fun getClanMember(name: String): ClanMember? {
        val player = Bukkit.getPlayerExact(name)
        return if (player != null) getClanMember(player)
        else clanMemberCache.members.firstOrNull { it.name.equals(name, true) }
    }

    fun getClanMember(player: Player): ClanMember {
        if (clanMemberCache.has(player.uniqueId)) return clanMemberCache.get(player.uniqueId)!!
        val member = ClanMember(
            player.uniqueId,
            player.name,
            null,
            null,
            TimeUtil.now()
        )
        clanMemberCache.put(player.uniqueId, member)
        Logger.instance.debug("Created a new clan member. (name: ${member.name}, uuid: ${member.uniqueId})")
        PluginCoroutineScope.scope.launch { repository.clanMemberDao.upsert(member) }
        return member
    }

    fun getClanByPlayer(player: Player) = clanMemberCache.get(player.uniqueId)?.clanId?.let { getClanByName(MessageService.raw(it)) }

    fun getClanByName(name: String) = clanCache.clans.firstOrNull { it.rawName().equals(name, true) }

    fun getClanInTop(place: Int) = clanCache.clans.sortedByDescending { it.level }.getOrNull(place)

    fun getClanByMember(member: ClanMember) = clanCache.clans.firstOrNull { it.has(member) }

    private fun randomHexColor(): String {
        val color = Random.nextInt(0x1000000)
        return String.format("#%06X", color)
    }
}