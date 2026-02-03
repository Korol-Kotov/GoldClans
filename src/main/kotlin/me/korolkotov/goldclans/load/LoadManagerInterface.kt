package me.korolkotov.goldclans.load

interface LoadManagerInterface<T> {
    fun getInstance(): T

    fun initialize()
    fun terminate() {}
    fun reload() {}
}