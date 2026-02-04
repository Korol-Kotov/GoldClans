package me.korolkotov.goldclans.database.repository

import me.korolkotov.goldclans.database.dao.ClanDao
import me.korolkotov.goldclans.database.dao.ClanMemberDao
import me.korolkotov.goldclans.database.dao.ClanStorageDao

data class ClanRepository(
    val clanDao: ClanDao,
    val clanMemberDao: ClanMemberDao,
    val clanStorageDao: ClanStorageDao
)