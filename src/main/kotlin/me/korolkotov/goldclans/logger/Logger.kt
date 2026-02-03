package me.korolkotov.goldclans.logger

import me.korolkotov.goldclans.config.ConfigManager
import java.util.Calendar

class Logger {
    companion object {
        lateinit var instance: Logger private set
    }

    private val logs = mutableListOf<Log>()

    init {
        instance = this
    }

    fun log(level: Level, text: String, throwable: Throwable? = null) {
        when (level) {
            Level.DEBUG -> debug(text, throwable)
            Level.INFO -> info(text, throwable)
            Level.WARN -> warn(text, throwable)
            Level.ERROR -> error(text, throwable)
        }
    }

    fun debug(text: String, throwable: Throwable? = null) {
        if (ConfigManager.instance.config.plugin.debug) {
            println(text)
            throwable?.printStackTrace()
        }

        logs.add(Log.create(text, Level.DEBUG))
        saveError(throwable)
    }

    fun debug(list: List<String>, throwable: Throwable? = null) {
        list.forEach { text -> debug(text, throwable) }
    }

    fun info(text: String, throwable: Throwable? = null) {
        println(text)
        throwable?.printStackTrace()

        logs.add(Log.create(text, Level.INFO))
        saveError(throwable)
    }

    fun info(list: List<String>, throwable: Throwable? = null) {
        list.forEach { text -> info(text, throwable) }
    }

    fun warn(text: String, throwable: Throwable? = null) {
        println(text)
        throwable?.printStackTrace()

        logs.add(Log.create(text, Level.WARN))
        saveError(throwable)
    }

    fun warn(list: List<String>, throwable: Throwable? = null) {
        list.forEach { text -> warn(text, throwable) }
    }

    fun error(text: String, throwable: Throwable? = null) {
        println(text)
        throwable?.printStackTrace()

        logs.add(Log.create(text, Level.ERROR))
        saveError(throwable)
    }

    fun error(list: List<String>, throwable: Throwable? = null) {
        list.forEach { text -> error(text, throwable) }
    }

    fun getLogs(): List<Log> {
        val logs = this.logs.toList()
        this.logs.clear()
        return logs
    }

    private fun saveError(throwable: Throwable?) {
        if (throwable == null || throwable.message == null) return
        logs.add(Log.create(throwable.message!!, Level.ERROR))
    }

    data class Log(
        val timestamp: Long,
        val text: String,
        val level: Level
    ) {
        companion object {
            fun create(text: String, level: Level): Log {
                val timestamp = Calendar.getInstance().timeInMillis / 1000
                return Log(timestamp, text, level)
            }
        }
    }
}