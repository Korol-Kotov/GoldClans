package me.korolkotov.goldclans.util

import me.korolkotov.goldclans.Main
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object TaskService {
    private val tasks = mutableListOf<CustomTask>()

    init {
        val thr = Thread {
            runCatching {
                while (!Thread.interrupted()) {
                    tasks.filter { it.bukkitTask.isCancelled }.forEach { task ->
                        task.bukkitTask.cancel()
                        tasks.remove(task)
                    }
                    Thread.sleep(1000)
                }
            }
        }
        thr.start()
    }

    fun cancelAll() {
        for (task in tasks.toList()) {
            task.bukkitTask.cancel()
            tasks.remove(task)
        }
        Bukkit.getScheduler().cancelTasks(Main.instance)
    }

    fun cancel(id: String) {
        val task = tasks.firstOrNull { it.id.equals(id, true) }
        if (task != null) {
            if (!task.bukkitTask.isCancelled) task.bukkitTask.cancel()
            tasks.remove(task)
        }
    }

    fun run(runnable: (BukkitRunnable) -> Unit) {
        object : BukkitRunnable() {
            override fun run() = runnable(this)
        }.runTask(Main.instance)
    }

    fun runLater(id: String, delay: Long, runnable: (String) -> Unit) {
        val task = object : BukkitRunnable() {
            override fun run() {
                runnable(id)
                cancel(id)
            }
        }.runTaskLater(Main.instance, delay)
        val customTask = CustomTask(id, task)
        tasks.add(customTask)
    }

    fun runTimer(id: String, delay: Long, period: Long, runnable: (String) -> Unit) {
        val task = object : BukkitRunnable() {
            override fun run() = runnable(id)
        }.runTaskTimer(Main.instance, delay, period)
        val customTask = CustomTask(id, task)
        tasks.add(customTask)
    }

    private data class CustomTask(
        val id: String,
        val bukkitTask: BukkitTask
    )
}