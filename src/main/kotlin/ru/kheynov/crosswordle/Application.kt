package ru.kheynov.crosswordle

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.kheynov.crosswordle.plugins.configureHTTP
import ru.kheynov.crosswordle.plugins.configureMonitoring
import ru.kheynov.crosswordle.plugins.configureRouting
import ru.kheynov.crosswordle.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT").toInt(), host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
