package ru.kheynov.crosswordle

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import ru.kheynov.crosswordle.generator.CrosswordGeneratorUtils.Companion.EMPTY_CELL
import ru.kheynov.crosswordle.generator.generateCrossword
import ru.kheynov.crosswordle.generator.shuffleWords
import java.time.LocalDateTime

typealias Cell = List<Char>

@Serializable
data class JsonCrossword(
    val day: Int,
    val crossword: List<Map<Int, Cell>>,
)

private fun generateCrossword(seed: Int, wordsStore: WordsStore): JsonCrossword {
    val crossword = generateCrossword(wordsStore.words, seed)
    val crosswordShuffled = shuffleWords(crossword, 11, seed)
    val cellsState: List<Map<Int, Cell>> = run {
        val res = mutableListOf<MutableMap<Int, Cell>>()
        for (i in crossword.indices) {
            if (crossword[i].count { it == EMPTY_CELL } == 7) continue
            res.add(mutableMapOf())
            for (j in crossword[i].indices) {
                if (crossword[i][j] == EMPTY_CELL) continue
                res[i][j] = listOf(crossword[i][j], crosswordShuffled[i][j])
            }
        }
        res
    }
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