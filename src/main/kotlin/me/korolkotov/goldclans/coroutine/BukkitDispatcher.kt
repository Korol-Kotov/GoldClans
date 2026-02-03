package me.korolkotov.goldclans.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import me.korolkotov.goldclans.Main
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

object BukkitDispatcher {
    val MAIN = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            Bukkit.getScheduler().runTask(Main.instance, block)
        }
    }
}