package ru.kheynov.crosswordle

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.kheynov.crosswordle.generator.CrosswordGeneratorUtils
import ru.kheynov.crosswordle.generator.generateCrossword
import ru.kheynov.crosswordle.generator.shuffleWords
import java.time.LocalDateTime

@Serializable
data class JsonCrossword(
    val day: Int,
    val crossword: List<Cell>,
)

@Serializable
data class Cell(
    val x: Int,
    val y: Int,
    val origin: Char,
    val current: Char,
)

private fun generateCrossword(seed: Int, wordsStore: WordsStore): JsonCrossword {
    val crossword = generateCrossword(wordsStore.words, seed)
    val crosswordShuffled = shuffleWords(crossword, 11, seed)
    val cellsState: List<Cell> = run {
        val res = mutableListOf<Cell>()
        for (i in 0 until CrosswordGeneratorUtils.GRID_SIZE) {
            for (j in 0 until CrosswordGeneratorUtils.GRID_SIZE) {
                res.add(Cell(i, j, crossword[i][j], crosswordShuffled[i][j]))
            }
        }
        res
    }
    println("seed: $seed")
    println("crossword: ${crossword.joinToString("")}")
    println("crosswordShuffled: ${crosswordShuffled.joinToString("")}")
    return JsonCrossword(
        day = LocalDateTime.now().run { dayOfYear },
        crossword = cellsState,
    )
}

fun Route.gameRoutes(wordsStore: WordsStore) {
    get("/daily") {
        val seed = System.getenv("SEED").toLong() + LocalDateTime.now().run { dayOfYear + year }.toLong()
        call.respond(
            HttpStatusCode.OK, generateCrossword(
                seed = seed.toInt(),
                wordsStore = wordsStore,
            )
        )
    }
    get("/practice") {
        val seed = System.getenv("SEED").toLong() + System.currentTimeMillis()
        call.respond(
            HttpStatusCode.OK, generateCrossword(
                seed = seed.toInt(),
                wordsStore = wordsStore,
            )
        )
    }
}