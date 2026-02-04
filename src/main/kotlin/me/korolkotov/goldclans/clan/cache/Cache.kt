package me.korolkotov.goldclans.clan.cache

import java.util.concurrent.ConcurrentHashMap

abstract class Cache<K, V> {
    protected val cache = ConcurrentHashMap<K, V>()

    fun get(key: K): V? = cache.getOrDefault(key, null)

    fun put(key: K, value: V) {
        cache[key] = value
    }

    fun remove(key: K) {
        cache.remove(key)
    }

    fun has(key: K) = cache.containsKey(key)
}