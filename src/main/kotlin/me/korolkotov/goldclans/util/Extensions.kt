package me.korolkotov.goldclans.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.math.BigDecimal
import java.math.RoundingMode

fun String.asComponent(): Component = LegacyComponentSerializer.legacyAmpersand().deserialize(this)

fun Double.format(): String {
    val bd = BigDecimal(this.toString())
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()

    return bd.toPlainString()
}