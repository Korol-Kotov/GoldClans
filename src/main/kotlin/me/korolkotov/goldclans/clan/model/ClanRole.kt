package me.korolkotov.goldclans.clan.model

enum class ClanRole(
    val id: Int
) {
    MEMBER(0),
    MODERATOR(1),
    LEADER(2)
}