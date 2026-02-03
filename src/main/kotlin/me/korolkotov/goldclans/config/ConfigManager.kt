package me.korolkotov.goldclans.config

import me.korolkotov.goldclans.Main
import me.korolkotov.goldclans.load.LoadManagerInterface
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager : LoadManagerInterface<ConfigManager> {
    companion object {
        lateinit var instance: ConfigManager private set
    }

    val dataFolder get() = Main.instance.dataFolder

    private val menuConfigs = mutableListOf<MenuConfig>()

    lateinit var config: GeneralConfig
    lateinit var databaseConfig: DatabaseConfig
    lateinit var messageConfig: MessageConfig

    init {
        instance = this
    }

    override fun getInstance() = this

    override fun initialize() {
        val config = loadOrCreate("config.yml")
        this.config = GeneralConfig(config)

        val database = loadOrCreate("database.yml")
        databaseConfig = DatabaseConfig(database.getConfigurationSection("database")!!)

        val languageFile = loadLanguageFile(this.config.plugin.language)
        messageConfig = MessageConfig(languageFile.getConfigurationSection("messages")!!)

        val menu = loadOrCreate("menu.yml")
        for (id in menu.getKeys(false)) {
            val section = menu.getConfigurationSection(id) ?: continue
            val menuConfig = MenuConfig(id, section)
            menuConfigs.add(menuConfig)
        }
    }

    override fun reload() {
        menuConfigs.clear()
        initialize()
    }

    fun getMenus() = menuConfigs.toList()

    private fun loadOrCreate(fileName: String, fill: Boolean = true): YamlConfiguration {
        val file = File(dataFolder, fileName)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            if (fill) {
                this::class.java.getResourceAsStream("/$fileName")?.use {
                    file.outputStream().use { out -> it.copyTo(out) }
                }
            }
            file.createNewFile()
        }
        return YamlConfiguration.loadConfiguration(file)
    }

    private fun loadLanguageFile(language: String): YamlConfiguration {
        return loadOrCreate("messages/$language.yml")
    }
}