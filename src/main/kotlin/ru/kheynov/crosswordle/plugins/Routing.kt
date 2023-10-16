package ru.kheynov.crosswordle.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.kheynov.crosswordle.WordsStore
import ru.kheynov.crosswordle.gameRoutes
import java.io.File

fun Application.configureRouting() {
    val wordsStore = WordsStore(File("files/words.txt"))
    routing {
        gameRoutes(wordsStore)
        get("/") {
            call.respondText("Crosswordle API")
        }
    }
}
