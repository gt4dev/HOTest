package gtr.hotest

import gtr.hotest.variants.VariantsRuntime
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass

class HOTestCtx {

    val hotestCtx: HOTestCtx = this
    internal var variantsRuntime: VariantsRuntime? = null

    private val items = mutableMapOf<String, Any>()
    private val koinApp = koinApplication { }
    val koin: Koin = koinApp.koin

    @Deprecated("Use directly `koin` ctx instead")
    inline operator fun <reified T : Any> set(key: String, value: T) {
        setInternal(key = key, value = value, type = T::class)
    }

    @PublishedApi
    internal fun setInternal(key: String, value: Any, type: KClass<*>) {
        if (items.containsKey(key)) {
            // forbid "map" modification - IT'S CRUCIAL for tests stability!!!
            throw IllegalArgumentException("key $key already exists")
        }
        items[key] = value
        koin.declare(
            instance = value,
            qualifier = named(key),
            secondaryTypes = listOf(type),
            allowOverride = true
        )
    }

    @Deprecated("Use `koin` ctx instead")
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T {
        if (!items.containsKey(key)) {
            throw IllegalArgumentException("key $key doesn't exist")
        }
        return items[key] as T
    }

    @Deprecated("Use `koin` ctx instead")
    fun containsKey(key: String): Boolean {
        return items.containsKey(key)
    }

    fun addToKoinTestModule(
        block: Module.() -> Unit
    ) {
        val createAtStart = false
        // koin doesn't allow to modify existing module
        // so each `block` is kept in "individual" module
        // seems strange, but it's ok - when koin loads modules list, it merges their content
        val testModule = module(
            createdAtStart = createAtStart,
            moduleDeclaration = block
        )
        koin.loadModules(
            modules = listOf(testModule),
            allowOverride = false,
            createEagerInstances = createAtStart
        )
    }
}