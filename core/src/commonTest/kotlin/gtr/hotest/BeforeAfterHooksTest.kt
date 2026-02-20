package gtr.hotest

import gtr.hotest.BeforeAfterHooksTest.Fixtures.assertNoObjectInCtx
import gtr.hotest.BeforeAfterHooksTest.Fixtures.assertObjectInCtx
import gtr.hotest.BeforeAfterHooksTest.Fixtures.setObjectInCtx
import gtr.hotest.variants.variant
import gtr.hotest.variants.variants
import org.koin.core.qualifier.named
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BeforeAfterHooksTest {

    private object Fixtures {
        fun HOTestCtx.setObjectInCtx(key: String, obj: Any) {
            this.koinAdd {
                single(named(key)) { obj }
            }
        }

        fun HOTestCtx.getObject(key: String): Any? {
            return this.koin.getOrNull(named(key))
        }

        fun HOTestCtx.assertNoObjectInCtx(key: String) {
            assertNull(getObject(key))
        }

        fun HOTestCtx.assertObjectInCtx(key: String, obj: Any) {
            assertEquals(obj, getObject(key))
        }
    }

    @Test
    fun case_basic() {
        val actionsLog = mutableListOf<String>()
        hotest(
            beforeTest = {
                actionsLog.add("before")

                assertNoObjectInCtx("sample1")
                setObjectInCtx("sample1", 123)
                assertObjectInCtx("sample1", 123)
            },
            afterTest = {
                actionsLog.add("after")
            }
        ) {
            actionsLog.add("inside")
        }

        val expected = listOf(
            "before", "inside", "after",
        )
        assertEquals(expected, actionsLog)
    }

    @Test
    fun case_with_simple_variants() {
        val actionsLog = mutableListOf<String>()
        hotest(
            beforeTest = {
                actionsLog.add("before")

                assertNoObjectInCtx("sample1")
                setObjectInCtx("sample1", 123)
                assertObjectInCtx("sample1", 123)
            },
            afterTest = { actionsLog.add("after") }
        ) {
            actionsLog.add("start")
            variants {
                variant { actionsLog.add("variant1") }
                variant { actionsLog.add("variant2") }
                variant { actionsLog.add("variant3") }
            }
            actionsLog.add("end")
        }

        val expected = listOf(
            // loop 1
            "before", "start", "variant1", "end", "after",
            // loop 2
            "before", "start", "variant2", "end", "after",
            // loop 3
            "before", "start", "variant3", "end", "after",
        )
        assertEquals(expected, actionsLog)
    }

    @Test
    fun case_with_subvariants() {
        val actionsLog = mutableListOf<String>()
        hotest(
            beforeTest = {
                actionsLog.add("before")

                assertNoObjectInCtx("sample1")
                setObjectInCtx("sample1", 123)
                assertObjectInCtx("sample1", 123)
            },
            afterTest = { actionsLog.add("after") }
        ) {
            actionsLog.add("start")
            variants {
                variant {
                    variants("vsA") {
                        variant {
                            actionsLog.add("vsA-v1")
                        }
                        variant {
                            actionsLog.add("vsA-v2")
                        }
                    }
                }
                variant {
                    variants("vsB") {
                        variant {
                            actionsLog.add("vsB-v1")
                        }
                        variant {
                            actionsLog.add("vsB-v2")
                        }
                    }
                }
            }
            actionsLog.add("end")
        }

        val expected = listOf(
            // loop 1
            "before", "start", "vsA-v1", "end", "after",
            // loop 2
            "before", "start", "vsA-v2", "end", "after",
            // loop 3
            "before", "start", "vsB-v1", "end", "after",
            // loop 4
            "before", "start", "vsB-v2", "end", "after",
        )
        assertEquals(expected, actionsLog)
    }
}