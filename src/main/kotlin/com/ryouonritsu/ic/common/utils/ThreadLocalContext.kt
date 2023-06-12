package com.ryouonritsu.ic.common.utils

/**
 * @author ryouonritsu
 */
object ThreadLocalContext {
    private val LOCAL: ThreadLocal<MutableMap<String, Any?>> =
        InheritableThreadLocal.withInitial(::hashMapOf)

    operator fun get(key: String) = LOCAL.get()[key]
    operator fun set(key: String, value: Any?) {
        LOCAL.get()[key] = value
    }

    fun remove(key: String) {
        LOCAL.get().remove(key)
    }

    fun clear() {
        LOCAL.get().clear()
        LOCAL.remove()
    }
}