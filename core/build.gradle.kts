plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.gt4dev"
version = "0.3.0"

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "hotest", version.toString())

    pom {
        name = "HOTest"
        description =
            "HOTest = Human Oriented Tests. Library help to create automated tests easy to read & write for humans. It simplify testing, enable TDD, BDD..."
        inceptionYear = "2026"
        url = "https://github.com/gt4dev/HOTest/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "gt4dev"
                name = "Kotlin Developer Advocate"
                url = "https://github.com/gt4dev/"
            }
        }
        scm {
            url = "https://github.com/gt4dev/HOTest/"
            connection = "scm:git:git://github.com/gt4dev/HOTest.git"
            developerConnection = "scm:git:ssh://git@github.com/gt4dev/HOTest.git"
        }
    }
}
