package dev.hotest.variants

import dev.hotest.HOTestCtx
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

        runtime.enterGroup(comment)
        try {
            this.testBody()
        } finally {
            val finished = runtime.exitGroup()
            runtime.finishGroup(finished)
        }
    }


    suspend fun HOTestCtx.variant(
        comment: String = "",
        testBody: suspend HOTestCtx.() -> Unit
    ) {
        val runtime = this.variantsRuntime
        val variantCtx = runtime?.registerVariant(comment)
        if (variantCtx == null) {
            this.testBody()
            return
        }

        if (variantCtx.shouldExecute) {
            runtime.enterVariant(variantCtx.option)
            try {
                this.testBody()
            } finally {
                runtime.exitVariant()
            }
        }
    }
}
