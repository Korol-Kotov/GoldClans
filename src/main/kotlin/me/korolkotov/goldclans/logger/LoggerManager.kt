package me.korolkotov.goldclans.logger

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.coroutine.PluginCoroutineScope
import me.korolkotov.goldclans.load.LoadManagerInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LoggerManager : LoadManagerInterface<LoggerManager> {
    private lateinit var folder: File

    private val logger = Logger()

    override fun getInstance() = this

    override fun initialize() {
        folder = File(ConfigManager.instance.dataFolder, "logs")
        if (!folder.exists()) folder.mkdirs()

        PluginCoroutineScope.scope.launch {
            while (true) {
                saveLogs()
                delay(1000 * 60)
            }
        }
        logger.debug("LoggerManager initialized.")
    }

    override fun terminate() {
        logger.debug("LoggerManager terminated.")
        saveLogs()
    }

    private fun saveLogs() {
        val logs = logger.getLogs()
        logs.forEach { log ->
            val date = Date(log.timestamp * 1000)
            val stringDate = dateToString("dd-MM-yyyy", date = date)
            val stringTime = dateToString("HH:mm:ss", date = date)
            val file = File(folder, "log-$stringDate.log")

            val text = "[$stringTime] ${log.level.prefix}: ${log.text}"

            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.writeText(text)
            } else {
                file.appendText("\n$text")
            }
        }
    }

    private fun dateToString(
        format: String,
        date: Date = Calendar.getInstance().time,
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(date)
    }
}