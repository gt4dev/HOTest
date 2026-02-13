package gtr.hotest.koin

import gtr.hotest.hotest
import gtr.hotest.variants.variant
import gtr.hotest.variants.variants
import org.koin.core.qualifier.named
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KoinContextTest {

    @Test
    fun handle_simple_cases() {
        hotest {

            variants {

                variant("many 'adds' in one call") {
                    addToKoinTestModule {
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
                    addToKoinTestModule {
                        single(named("callA name1")) { "callA sample text 1" }
                        single(named("callA name2")) { "callA sample text 2" }
                    }

                    addToKoinTestModule {
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
    fun handle_complex_object_cases() {
        hotest {
            variants {

                variant("object already ready") {
                    addToKoinTestModule {
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
                            addToKoinTestModule {
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
                            addToKoinTestModule {
                                factory {
                                    Person(
                                        name = "person XXX",
                                        address = get()
                                    )
                                }
                            }
                            addToKoinTestModule {
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
    fun handle_edge_cases() {
        hotest {

            variants {

                variant("lack of 'root object' definition (Person)") {
                    addToKoinTestModule {
                        // no definitions
                    }
                    assertFailsWith<org.koin.core.error.NoDefinitionFoundException> {
                        val person: Person = koin.get()
                        person.toString()
                    }
                }

                variant("lack of 'dependent object' definition (Address)") {
                    addToKoinTestModule {
                        factory {
                            Person(
                                name = "person XXX",
                                address = get()
                            )
                        }
                    }
                    assertFailsWith<org.koin.core.error.InstanceCreationException> {
                        val person: Person = koin.get()
                        person.toString()
                    }
                }

                variant("duplicated definition of 'root object' (Person)") {
                    assertFailsWith<org.koin.core.error.DefinitionOverrideException> {
                        addToKoinTestModule {
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
                        addToKoinTestModule {
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