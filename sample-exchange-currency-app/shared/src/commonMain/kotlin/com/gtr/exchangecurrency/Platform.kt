package com.gtr.exchangecurrency

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform