# HOTest = Human Oriented Tests  

*NOTE: HOTest is under heavy development.*  
*The library implementation is at an early stage and the API is changing, but the pattern is stable.*

## Introduction

HOTest is a testing pattern and library that blends Gherkin-like readability with xUnit-style tests.  
You write scenarios in human-friendly language, but still in code, which keeps tests close to the implementation while avoiding tight coupling of tests and production.  

### Main HOTest traits
- Focus on creating human-readable tests. 
- Reusable test steps make writing tests much easier.


### Why use HOTest
- Business rules are expressed in the most human-friendly way, with minimal technical details.
- Readability of tests / specification has the highest priority.
- Readable specification / tests:
  - make changes in business logic easier
  - enable better understanding of business opportunities, leading to evolution.
- In HOTest, business logic is the top priority; implementation is a technical detail.
  - You can truly start with tests (TDD) or specification (BDD) and later derive production code from it.
- Loose coupling between tests and production code prevents architecture from becoming rigid.
  - You can change architecture of the solution, even shift programming paradigms, still keeping business rules untouched.
  - You can easily change implementation details without changing business rules.
- Clearer architecture through separation between what should happen (test) and how it happens (production code).
- Reusable steps speed up writing new scenarios and make exploring many variants of a scenario easier.

### When Not to Use HOTest  
When tests must verify low-level implementation details or exact API calls.  
It's worth using HOTest only when you have human-readable business requirements.


## Quick Start Example  

Sample test:
```kotlin
@Test
fun `exchange currencies - direct rate use`() {
    hotest {
        `given fake rates service returns`(
            ExchangeRate("EUR", "PLN", 4),
            ExchangeRate("EUR", "CHF", 2),
            ExchangeRate("EUR", "USD", 1),
        )
        `when exchange calculator converts`(
            Money(10, "EUR"),
            Currency.PLN
        )
        `then exchange calculator returns`(
            Money(40, "PLN"),
        )
    }
}

@Test
fun `exchange currencies - reversed rate use`() {
    hotest {
        `given fake rates service returns`(
            ExchangeRate("EUR", "PLN", 4),
            ExchangeRate("EUR", "CHF", 2),
            ExchangeRate("EUR", "USD", 1),
        )
        `when exchange calculator converts`(
            Money(40, "PLN"),
            Currency.EUR
        )
        `then exchange calculator returns`(
            Money(10, "EUR"),
        )
    }
}
```

Notes about the example: 
- Steps express intent, not implementation. Tests say what should happen, not which methods to call.
- Steps use human-language names (Gherkin-style `given / when / then`), are reusable, and easy to read.
- Steps are called inside `hotest {}`, which sets up scenario context between steps.
- The test uses a standard `@Test` annotation.

**Any test** framework can run HOTest scenarios: JUnit, Kotest, Kotlin test, etc.

Sample step definition:
```kotlin
fun HOTestCtx.`then exchange calculator returns`(
    money: Models.Money,
) {
    val result: Money = this[KEY_RESULTS]
    Assertions.moneyEquals(money, result)
}
```
To keep context between step calls, all steps use `HOTestCtx`.  
`HOTestCtx` stores SUT objects and all data required by the scenario and shared between steps.

## Test Scenario Variants  
`variants {}` reduce boilerplate when you want several scenario variations without duplicating shared parts.

**How variants execute**
- `variants {}` defines a block with multiple `variant {}` branches.
- Each `variant {}` is executed in a separate run of the surrounding test.
- Traversal order follows depth-first search (graph's DFS algorithm) through nested variants.

**Example**
```kotlin
hotest {
    println("step on start")
    variants {
        variant { println("variant1") }
        variant { println("variant2") }
        variant { println("variant3") }
    }
    println("step on end")
}
```

Expected output:
```text
// 1st loop
step on start
variant1
step on end

// 2nd loop
step on start
variant2
step on end

// 3rd loop
step on start
variant3
step on end
```

**Rules for using variants**
1. You can define any number of `variant {}` blocks inside a `variants {}` block.
1. Only one `variants` block is allowed at a given test level.
1. `variants` can be nested inside other `variants` blocks, as shown below:

```kotlin
// example of nested variants

hotest {

    // call steps common for ALL variants
    // ...

    // 1st level of variants
    variants("variants for different nutrition") {

        variant("proteins") {

            // call steps common in "proteins" related scenarios

            // 2nd level of variants      
            variants("variants for different proteins") {
                variant("proteins from vegetables") {
                    // call steps related only to this variant
                }
                variant("proteins from meat") {
                    // call steps for this variant
                }
            }
        }
        variant("fats") { ... }
        variant("carbs") { ... }
    }
}
```

## Real usage example
For advanced, full examples of HOTest usage, refer to the [Multi Project Focus](https://github.com/gt4dev/MultiProjectFocus) project.  

1. Sample tests: [main/composeApp/src/commonTest](https://github.com/gt4dev/MultiProjectFocus/tree/main/composeApp/src/commonTest/kotlin/gtr/mpfocus)    
1. HOTest integration with your project:  
   Temporary integration uses [Gradle composite build](https://github.com/gt4dev/MultiProjectFocus/blob/main/settings.gradle.kts).
