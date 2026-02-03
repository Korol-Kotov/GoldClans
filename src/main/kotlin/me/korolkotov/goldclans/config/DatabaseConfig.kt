package me.korolkotov.goldclans.config

import org.bukkit.configuration.ConfigurationSection

class DatabaseConfig(section: ConfigurationSection) {
    val type = section.getString("type")!!

    val mysql = MySQLConfig(section.getConfigurationSection("mysql")!!)
    val sqlite = SQLiteConfig(section.getConfigurationSection("sqlite")!!)
}

class MySQLConfig(section: ConfigurationSection) {
    val host = section.getString("host")!!
    val port = section.getString("port")!!
    val database = section.getString("database")!!
    val user = section.getString("user")!!
    val password = section.getString("password")!!

    val connectionProperties = ConnectionPropertiesConfig(section.getConfigurationSection("connection-properties")!!)
    val pool = PoolConfig(section.getConfigurationSection("pool")!!)
}

class SQLiteConfig(section: ConfigurationSection) {
    val file = section.getString("file")!!
}

class ConnectionPropertiesConfig(section: ConfigurationSection) {
    val useSSL = section.getBoolean("useSSL")
    val autoReconnect = section.getBoolean("autoReconnect")
    val characterEncoding = section.getString("characterEncoding")
    val cachePrepStmts = section.getBoolean("cachePrepStmts")
    val prepStmtCacheSize = section.getInt("prepStmtCacheSize")
    val prepStmtCacheSqlLimit = section.getInt("prepStmtCacheSqlLimit")
}

class PoolConfig(section: ConfigurationSection) {
    val poolSize = section.getInt("pool-size")
    val minimumIdle = section.getInt("minimum-idle")
    val idleTimeout = section.getLong("idle-timeout")
    val connectionTimeout = section.getLong("connection-timeout")
    val maxLifetime = section.getLong("max-lifetime")
}