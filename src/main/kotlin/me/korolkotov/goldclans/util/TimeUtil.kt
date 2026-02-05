package me.korolkotov.goldclans.util

import java.time.Clock

object TimeUtil {
    fun now() = Clock.systemUTC().instant()!!
}