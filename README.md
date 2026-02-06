# HOTest = Human Oriented Tests

## Introduction

HOTest is a testing pattern and library that blends Gherkin-like readability with xUnit-style tests.  
You write scenarios in human-friendly language, but still in code, which keeps tests close to the implementation while avoiding tight coupling of tests and production.

**NOTE: HOTest is under heavy development:**
- the implementation is at an early stage and the API is changing,
- the concept is stable.

**Why HOTest**
- Tests read like business scenarios, not implementation calls.
- Reusable steps speed up writing new test scenarios.
- Loose coupling between tests and production code reduces the freezing of production code evolution.
- Clear architecture through separation between what should happen (test intent) and how it happens (production implementation).

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

You can find details in the project `MultiProjectFocus` sources.

Notes about the example: 
- Steps express intent, not implementation. Tests say what should happen, not which methods to call.
- Steps use human-language names (Gherkin-style `given / when / then`), are reusable, and easy to read.
- Steps are called inside `hotest {}` which set up scenario context between steps.
- The test uses a classic `@Test` starter.

Note that **ANY** tests framework can run HOTest scenarios: JUnit, Kotest, Kotlin test, etc.

Sample step definition:
```kotlin
fun HOTestCtx.`then exchange calculator returns`(
    money: Models.Money,
) {
    val result: Money = this[KEY_RESULTS]
    Assertions.moneyEquals(money, result)
}
```
Note that to keep context between step calls - all steps use `HOTestCtx`.  
In `HOTestCtx` are kept SUT objects and all data required by test scenario and passed between steps.

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
1. You can define any number of `variant` inside a `variants` block.
1. Only one `variants` block is allowed at a given test level.
1. `variants` can be nested inside other `variants` blocks - like shown here:

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

# Others

### Real usage example  

Instruction on how to set up your project to use HOTest and real-world scenarios you can find in associated project [Multi Project Focus](https://github.com/gt4dev/MultiProjectFocus)

### When Not to Use HOTest
When tests must verify low-level implementation details or exact API calls.
It's worth using HOTest when you have pure business requirements.
