package me.korolkotov.goldclans.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

object PluginCoroutineScope {
    val scope = CoroutineScope(
        SupervisorJob()
    )

    fun shutdown() {
        scope.cancel()
    }
}