package gtr.hotest.variants

import gtr.hotest.HOTestCtx
import kotlinx.coroutines.runBlocking

fun HOTestCtx.variants(
    comment: String = "",
    testBody: HOTestCtx.() -> Unit
) = runBlocking {
    val hotestCtx = this@variants
    with(Async) {
        hotestCtx.variants { testBody() }
    }
}

fun HOTestCtx.variant(
    comment: String = "",
    testBody: HOTestCtx.() -> Unit
) = runBlocking {
    val hotestCtx = this@variant
    with(Async) {
        hotestCtx.variant { testBody() }
    }
}


object Async {

    suspend fun HOTestCtx.variants(
        comment: String = "",
        testBody: suspend HOTestCtx.() -> Unit
    ) {
        val runtime = this.variantsRuntime
        if (runtime == null) {
            this.testBody()
            return
        }

        val blockIndex = runtime.nextBlockIndex()
        runtime.enterBlock(blockIndex)
        try {
            this.testBody()
        } finally {
            val finished = runtime.exitBlock()
            runtime.finishBlock(finished)
        }
    }


    suspend fun HOTestCtx.variant(
        comment: String = "",
        testBody: suspend HOTestCtx.() -> Unit
    ) {
        val runtime = this.variantsRuntime
        val ctx = runtime?.currentBlock()
        if (ctx == null) {
            this.testBody()
            return
        }

        val index = ctx.variantCount
        ctx.variantCount += 1
        if (index == ctx.chosenIndex) {
            this.testBody()
        }
    }
}
