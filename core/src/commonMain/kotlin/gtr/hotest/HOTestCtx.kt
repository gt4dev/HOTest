package gtr.hotest

import gtr.hotest.variants.VariantsRuntime

class HOTestCtx {

    val hotestCtx: HOTestCtx = this
    internal var variantsRuntime: VariantsRuntime? = null

    private val items = mutableMapOf<String, Any>()

    operator fun <T> set(key: String, value: T) {
        if (items.containsKey(key)) {
            // forbid "map" modification - IT'S CRUCIAL for tests stability!!!
            throw IllegalArgumentException("key $key already exists")
        }
        items[key] = value as Any
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T {
        if (!items.containsKey(key)) {
            throw IllegalArgumentException("key $key doesn't exist")
        }
        return items[key] as T
    }

    fun containsKey(key: String): Boolean {
        return items.containsKey(key)
    }
}


fun setupHotestCtx(
    hc: HOTestCtx = HOTestCtx(),
    setup: HOTestCtx.() -> Unit
): HOTestCtx {
    hc.setup()
    return hc
}