package me.korolkotov.goldclans.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.load.LoadManagerInterface
import me.korolkotov.goldclans.logger.Logger
import java.io.File

class DatabaseManager : LoadManagerInterface<DatabaseManager> {
    lateinit var dataSource: HikariDataSource

    override fun getInstance() = this

    override fun initialize() {
        val hikari = if (ConfigManager.instance.databaseConfig.type.equals("mysql", true)) {
            val mysql = ConfigManager.instance.databaseConfig.mysql
            HikariConfig().apply {
                jdbcUrl =
                    "jdbc:mysql://${mysql.host}:${mysql.port}/${mysql.database}" +
                            "?useSSL=${mysql.connectionProperties.useSSL}" +
                            "&autoReconnect=${mysql.connectionProperties.autoReconnect}" +
                            "&characterEncoding=${mysql.connectionProperties.characterEncoding}" +
                            "&cachePrepStmts=${mysql.connectionProperties.cachePrepStmts}" +
                            "&prepStmtCacheSize=${mysql.connectionProperties.prepStmtCacheSize}" +
                            "&prepStmtCacheSqlLimit=${mysql.connectionProperties.prepStmtCacheSqlLimit}" +
                            "&serverTimezone=UTC"

                username = mysql.user
                password = mysql.password

                maximumPoolSize = mysql.pool.poolSize
                minimumIdle = mysql.pool.minimumIdle

                idleTimeout = mysql.pool.idleTimeout
                connectionTimeout = mysql.pool.connectionTimeout
                maxLifetime = mysql.pool.maxLifetime

                driverClassName = "com.mysql.cj.jdbc.Driver"
            }
        } else {
            val sqlite = ConfigManager.instance.databaseConfig.sqlite
            val dbFile = File(ConfigManager.instance.dataFolder, sqlite.file)
            HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"

                maximumPoolSize = 1
                minimumIdle = 1
                idleTimeout = 0
                maxLifetime = 0
                connectionTimeout = 30_000

                isAutoCommit = true

                driverClassName = "org.sqlite.JDBC"
            }
        }

        dataSource = HikariDataSource(hikari)
        Logger.instance.debug("Data source has been initialized.")

        MigrationService(dataSource).migrate()
        Logger.instance.debug("Database has been migrated.")
    }

    override fun terminate() {
        dataSource.close()
    }
}