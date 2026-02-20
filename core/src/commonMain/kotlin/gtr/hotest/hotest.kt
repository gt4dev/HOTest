package gtr.hotest

import gtr.hotest.variants.VariantsRuntime
import kotlinx.coroutines.runBlocking

fun hotest(
    createCtx: () -> HOTestCtx = { HOTestCtx() },
    beforeTest: HOTestCtx.() -> Unit = {},
    afterTest: HOTestCtx.() -> Unit = {},
    testBody: HOTestCtx.() -> Unit = {}
): HOTestCtx = runBlocking {
    Async.hotest(createCtx, beforeTest, afterTest, testBody)
}

object Async {

    suspend fun hotest(
        createCtx: suspend () -> HOTestCtx = { HOTestCtx() },
        beforeTest: suspend HOTestCtx.() -> Unit = {},
        afterTest: suspend HOTestCtx.() -> Unit = {},
        testBody: suspend HOTestCtx.() -> Unit = {}
    ): HOTestCtx {
        val runtime = VariantsRuntime()
        runtime.resetForHotest()
        var lastCtx: HOTestCtx? = null

        while (runtime.hasPendingRuns()) {
            val selection = runtime.nextSelection()
            runtime.startRun(selection)
            val hotestCtx = createCtx()
            hotestCtx.beforeTest()
            val previousRuntime = hotestCtx.variantsRuntime
            hotestCtx.variantsRuntime = runtime
            lastCtx = hotestCtx
            try {
                hotestCtx.testBody()
            } finally {
                try {
                    hotestCtx.afterTest()
                } finally {
                    hotestCtx.variantsRuntime = previousRuntime
                }
            }
        }
        return requireNotNull(lastCtx)
    }
}
