package gtr.hotest

import gtr.hotest.variants.VariantsRuntime
import kotlinx.coroutines.runBlocking

fun hotest(
    beforeTest: () -> HOTestCtx = { HOTestCtx() },
    afterTest: (HOTestCtx) -> Unit = {},
    testBody: HOTestCtx.() -> Unit
): HOTestCtx = runBlocking {
    Async.hotest(beforeTest, afterTest) { testBody() }
}

object Async {

    suspend fun hotest(
        // beforeTest - creates HOTestCtx, called on each start of the 'test loop'
        beforeTest: () -> HOTestCtx = { HOTestCtx() },
        // afterTest - called on each end of the 'test loop'
        afterTest: (HOTestCtx) -> Unit = {},
        testBody: suspend HOTestCtx.() -> Unit
    ): HOTestCtx {
        val runtime = VariantsRuntime()
        runtime.resetForHotest()
        var lastCtx: HOTestCtx? = null

        while (runtime.hasPendingRuns()) {
            val selection = runtime.nextSelection()
            runtime.startRun(selection)
            val hotestCtx = beforeTest()
            val previousRuntime = hotestCtx.variantsRuntime
            hotestCtx.variantsRuntime = runtime
            lastCtx = hotestCtx
            try {
                hotestCtx.testBody()
            } finally {
                try {
                    afterTest(hotestCtx)
                } finally {
                    hotestCtx.variantsRuntime = previousRuntime
                }
            }
        }
        return requireNotNull(lastCtx)
    }
}
