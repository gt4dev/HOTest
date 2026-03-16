package gtr.hotest.variants

import gtr.hotest.hotest
import kotlin.test.Test
import kotlin.test.assertEquals

class VariantsTest {

    @Test
    fun `test simple case - variants with one level`() {
        val result = mutableListOf<String>()
        hotest {
            result.add("start")
            variants("vsA") {
                variant("vsA-v1") { result.add("vsA-v1") }
                variant("vsA-v2") { result.add("vsA-v2") }
                variant("vsA-v3") { result.add("vsA-v3") }
            }
            result.add("end")
        }

        val expected = listOf(
            // loop 1
            "start", "vsA-v1", "end",
            // loop 2
            "start", "vsA-v2", "end",
            // loop 3
            "start", "vsA-v3", "end",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test empty 'variants' - without nested 'variant'`() {
        val result = mutableListOf<String>()
        hotest {
            result.add("start")
            variants("vsA") {
                result.add("vsA")
            }
            result.add("end")
        }

        val expected = listOf(
            // loop 1
            "start", "vsA", "end",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test two levels variant - regular case`() {
        val result = mutableListOf<String>()
        hotest {
            result.add("start")
            variants("vsA") {
                variant("vsA-v1") {
                    variants("vsB") {
                        variant("vsB-v1") {
                            result.add("vsB-v1")
                        }
                        variant("vsB-v2") {
                            result.add("vsB-v2")
                        }
                    }
                }
                variant("vsA-v2") {
                    variants("vsC") {
                        variant("vsC-v1") {
                            result.add("vsC-v1")
                        }
                        variant("vsC-v2") {
                            result.add("vsC-v2")
                        }
                    }
                }
            }
            result.add("end")
        }

        val expected = listOf(
            // loop 1
            "start", "vsB-v1", "end",
            // loop 2
            "start", "vsB-v2", "end",
            // loop 3
            "start", "vsC-v1", "end",
            // loop 4
            "start", "vsC-v2", "end",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `many 'variants' - each with nested 'variant'`() {
        val result = mutableListOf<String>()
        hotest {
            result.add("start")
            variants("vsA") {
                variant("vsA-v1") {
                    variants("vsB") {
                        variant("vsB-v1") {
                            result.add("vsB-v1")
                        }
                    }
                }
                variant("vsA-v2") {
                    variants("vsC") {
                        variant("vsC-v1") {
                            result.add("vsC-v1")
                        }
                    }
                }
            }
            result.add("end")
        }

        val expected = listOf(
            // loop 1
            "start", "vsB-v1", "end",
            // loop 2
            "start", "vsC-v1", "end",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `variants with different number of nested siblings variants`() {
        val result = mutableListOf<String>()
        hotest {
            result.add("start")
            variants("vsA") {
                variant("vsA-v1") {
                    variants("vsB") {
                        variant("vsB-v1") {
                            result.add("vsB-v1")
                        }
                        variant("vsB-v2") {
                            result.add("vsB-v2")
                        }
                    }
                }
                variant("vsA-v2") {
                    variants("vsC") {
                        variant("vsC-v1") {
                            result.add("vsC-v1")
                        }
                    }
                }
            }
            result.add("end")
        }

        val expected = listOf(
            // loop 1
            "start", "vsB-v1", "end",
            // loop 2
            "start", "vsB-v2", "end",
            // loop 3
            "start", "vsC-v1", "end",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `test order of visiting nodes`() {
        val result = mutableListOf<String>()
        hotest {
            result.add("start")
            variants("A") {
                variant("A1") {
                    variants("B") {
                        variant("B1") {
                            result.add("B1")
                        }
                        variant("B2") {
                            result.add("B2")
                        }
                    }
                    variants("C") {
                        variant("C1") {
                            variants("E") {
                                variant("E1") {
                                    result.add("E1")
                                }
                                variant("E2") {
                                    result.add("E2")
                                }
                            }
                        }
                        variant("C2") {
                            result.add("C2")
                        }
                    }
                }
                variant("A2") {
                    variants("D") {
                        variant("D1") {
                            result.add("D1")
                        }
                        variant("D2") {
                            result.add("D2")
                        }
                    }
                }
            }
        }

        val expected = listOf(
            "start", "B1",
            "start", "B2",
            "start", "E1",
            "start", "E2",
            "start", "C2",
            "start", "D1",
            "start", "D2",
        )
        assertEquals(expected, result)
    }


    @Test
    fun `test precise traverse through complex variants tree`() {
        val result = mutableListOf<String>()
        hotest {
            result.add("start")
            variants("vsA") {
                result.add("vsA-start")
                variant("vsA-v1") {
                    result.add("vsA-v1-start")
                    variants("vsB") {
                        result.add("vsB-start")
                        variant("vsB-v1") {
                            result.add("vsB-v1")
                        }
                        result.add("vsB-mid")
                        variant("vsB-v2") {
                            result.add("vsB-v2")
                        }
                        result.add("vsB-end")
                    }
                    result.add("vsA-v1-end")
                }
                result.add("vsA-mid")
                variant("vsA-v2") {
                    result.add("vsA-v2-start")
                    variants("vsC") {
                        result.add("vsC-start")
                        variant("vsC-v1") {
                            result.add("vsC-v1")
                        }
                        result.add("vsC-mid")
                        variant("vsC-v2") {
                            result.add("vsC-v2")
                        }
                        result.add("vsC-end")
                    }
                    result.add("vsA-v2-end")
                }
                result.add("vsA-end")
            }
            result.add("end")
        }

        // variants tree:
        // root {
        //     variants("vsA") {
        //         variant("vsA-v1") {
        //             variants("vsB") {
        //                 variant("vsB-v1") { }
        //                 variant("vsB-v2") { }
        //             }
        //         }
        //         variant("vsA-v2") {
        //             variants("vsC") {
        //                 variant("vsC-v1") { }
        //                 variant("vsC-v2") { }
        //             }
        //         }
        //     }
        // }

        // @formatter:off
        val expected = listOf(
            // loop 1
             "start", "vsA-start", "vsA-v1-start", "vsB-start", "vsB-v1", "vsB-mid", "vsB-end", "vsA-v1-end", "vsA-mid", "vsA-end", "end",
            // loop 2
             "start", "vsA-start", "vsA-v1-start", "vsB-start", "vsB-mid", "vsB-v2", "vsB-end", "vsA-v1-end", "vsA-mid", "vsA-end", "end",
            // loop 3
             "start", "vsA-start", "vsA-mid", "vsA-v2-start", "vsC-start", "vsC-v1", "vsC-mid", "vsC-end", "vsA-v2-end", "vsA-end", "end",
            // loop 4
             "start", "vsA-start", "vsA-mid", "vsA-v2-start", "vsC-start", "vsC-mid", "vsC-v2", "vsC-end", "vsA-v2-end", "vsA-end", "end",
        )
        // @formatter:on
        assertEquals(expected, result)
    }
}
