package me.korolkotov.goldclans.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

fun String.asComponent(): Component = LegacyComponentSerializer.legacyAmpersand().deserialize(this)

fun ResultSet.getInstant(column: String): Instant = getTimestamp(column).toInstant()

fun PreparedStatement.setInstant(index: Int, instant: Instant) = setTimestamp(index, Timestamp.from(instant))

fun Double.format(): String {
    val bd = BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()

    return bd.toPlainString()
}