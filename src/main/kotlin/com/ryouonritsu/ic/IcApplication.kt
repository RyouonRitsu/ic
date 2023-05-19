package com.ryouonritsu.ic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class IcApplication

fun main(args: Array<String>) {
    runApplication<IcApplication>(*args)
}
