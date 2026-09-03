package dev.hotest.old_ver0.koin

import dev.hotest.old_ver0.hotest
import dev.hotest.old_ver0.variants.variant
import dev.hotest.old_ver0.variants.variants
import org.koin.core.error.DefinitionOverrideException
import org.koin.core.error.InstanceCreationException
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.qualifier.named
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KoinContextTest {

    @Test
    fun `test simple object`() {
        hotest {

            variants {

                variant("many 'adds' in one call") {
                    koinAdd {
                        single(named("name1")) { "sample text 1" }
                        single(named("name2")) { "sample text 2" }
                        single(named("name3")) { "sample text 3" }
                    }

                    for (idx in 1..3) {
                        assertEquals(
                            "sample text $idx",
                            koin.get(named("name$idx"))
                        )
                    }
                }

                variant("many 'adds' in many calls") {
                    koinAdd {
                        single(named("callA name1")) { "callA sample text 1" }
                        single(named("callA name2")) { "callA sample text 2" }
                    }

                    koinAdd {
                        single(named("callB name1")) { "callB sample text 1" }
                        single(named("callB name2")) { "callB sample text 2" }
                    }

                    for (call in listOf("callA", "callB")) {
                        for (idx in 1..2) {
                            assertEquals(
                                "$call sample text $idx",
                                koin.get(named("$call name$idx"))
                            )
                        }
                    }
                }
            }
        }
    }


    class Person(
        val name: String,
        val address: Address
    )

    class Address(
        val country: String,
        val city: String,
    )


    @Test
    fun `test complex object`() {
        hotest {
            variants {

                variant("object already ready") {
                    koinAdd {
                        single {
                            Person(
                                name = "person 123",
                                address = Address(
                                    country = "country 123",
                                    city = "city 123"
                                )
                            )
                        }
                    }
                    val person: Person = koin.get()
                    assertEquals("person 123", person.name)
                }

                variant("object factored by koin") {

                    variants {

                        variant("one call of 'addToKoinTestModule'") {
                            koinAdd {
                                factory {
                                    Person(
                                        name = "person AAA",
                                        address = get()
                                    )
                                }
                                single {
                                    Address(
                                        country = "country 123",
                                        city = "city 123"
                                    )
                                }
                            }
                            val person: Person = koin.get()
                            assertEquals("person AAA", person.name)
                        }

                        variant("many call of 'addToKoinTestModule'") {
                            koinAdd {
                                factory {
                                    Person(
                                        name = "person XXX",
                                        address = get()
                                    )
                                }
                            }
                            koinAdd {
                                single {
                                    Address(
                                        country = "country 123",
                                        city = "city 123"
                                    )
                                }
                            }
                            val person: Person = koin.get()
                            assertEquals("person XXX", person.name)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `test edge cases`() {
        hotest {

            variants {

                variant("lack of 'root object' definition (Person)") {
                    koinAdd {
                        // no definitions
                    }
                    assertFailsWith<NoDefinitionFoundException> {
                        val person: Person = koin.get()
                        person.toString()
                    }
                }

                variant("lack of 'dependent object' definition (Address)") {
                    koinAdd {
                        factory {
                            Person(
                                name = "person XXX",
                                address = get()
                            )
                        }
                    }
                    assertFailsWith<InstanceCreationException> {
                        val person: Person = koin.get()
                        person.toString()
                    }
                }

                variant("duplicated definition of 'root object' (Person)") {
                    assertFailsWith<DefinitionOverrideException> {
                        koinAdd {
                            factory {
                                Person(
                                    name = "person XXX1",
                                    address = get()
                                )
                            }
                            factory { // n: this duplication doesn't crash, in 1 module koin allows duplications
                                Person(
                                    name = "person XXX2",
                                    address = get()
                                )
                            }
                            factory {
                                Address(
                                    country = "country 123",
                                    city = "city 123"
                                )
                            }
                        }
                        koinAdd {
                            factory {  // n: this duplication crashes, because it's repeats above definition
                                Person(
                                    name = "person XXX",
                                    address = get()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}
