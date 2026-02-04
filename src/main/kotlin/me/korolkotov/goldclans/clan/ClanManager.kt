package me.korolkotov.goldclans.clan

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.korolkotov.goldclans.clan.cache.ClanCache
import me.korolkotov.goldclans.clan.cache.ClanMemberCache
import me.korolkotov.goldclans.coroutine.BukkitDispatcher
import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.database.DatabaseManager
import me.korolkotov.goldclans.database.repository.ClanRepository
import me.korolkotov.goldclans.load.LoadManager
import me.korolkotov.goldclans.load.LoadManagerInterface
import me.korolkotov.goldclans.logger.Logger

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
                clanCache.put(clan.name, clan)
            }
            withContext(BukkitDispatcher.MAIN) {
                Logger.instance.debug("Clans have been loaded to the cache")
            }

            repository.clanMemberDao.findAll().forEach { member ->
                clanMemberCache.put(member.uniqueId, member)
            }
            withContext(BukkitDispatcher.MAIN) {
                Logger.instance.debug("Clans members have been loaded to the cache")
            }
        }
    }
}