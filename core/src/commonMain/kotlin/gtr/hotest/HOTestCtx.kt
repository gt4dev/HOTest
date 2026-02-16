package gtr.hotest

import gtr.hotest.variants.VariantsRuntime
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class HOTestCtx {

    val hotestCtx: HOTestCtx = this
    internal var variantsRuntime: VariantsRuntime? = null

    private val koinApp = koinApplication { }
    val koin: Koin = koinApp.koin

    /**
     * Register 'object declaration' in HOTest koin test module.
     */
    fun koinAdd(
        block: Module.() -> Unit
    ) {
        val createAtStart = false
        // koin doesn't allow to modify existing module
        // so each `block` is kept in "individual" module
        // seems strange, but it's ok - when koin finally creates objects, it just merges modules content
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