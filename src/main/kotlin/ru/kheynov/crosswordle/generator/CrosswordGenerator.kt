package ru.kheynov.crosswordle.generator

import java.io.File
import kotlin.random.Random

data class Word(
    var text: String,
    var row: Int,
    var column: Int,
    var isVertical: Boolean,
)

class CrosswordGeneratorUtils {
    companion object {
        const val EMPTY_CELL: Char = '_'
        const val ATTEMPTS_TO_FIT_WORDS = 50000
        const val GRIDS_TO_MAKE = 100
        const val GRID_SIZE: Int = 7
    }

    private val grid = Array(GRID_SIZE) { CharArray(GRID_SIZE) }

    fun getGrid(): Array<CharArray> = grid

    init {
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                grid[i][j] = EMPTY_CELL
            }
        }
    }

    private fun isValidPosition(row: Int, column: Int): Boolean = row in 0..<GRID_SIZE && column in 0..<GRID_SIZE

    fun tryUpdate(word: Word): Boolean {
        return if (canBePlaced(word)) {
            addWord(word)
            true
        } else false
    }

    private fun addWord(word: Word) {
        for (letterIndex in 0 until word.text.length) {
            var row = word.row
            var column = word.column
            if (word.isVertical) {
                row += letterIndex
            } else {
                column += letterIndex
            }
            grid[row][column] = word.text.substring(letterIndex, letterIndex + 1)[0]
        }
    }

    private fun canBePlaced(word: Word): Boolean {
        var canBePlaced = true
        if (isValidPosition(word.row, word.column) && fitsOnGrid(word)) {
            var index = 0
            while (index < word.text.length) {
                val currentRow = if (word.isVertical) word.row + index else word.row
                val currentColumn = if (!word.isVertical) word.column + index else word.column
                if (!((word.text[index] == grid[currentRow][currentColumn] || EMPTY_CELL == grid[currentRow][currentColumn]) && isPlacementLegal(
                        word, currentRow, currentColumn
                    ))
                ) {
                    canBePlaced = false
                }
                index++
            }
        } else {
            canBePlaced = false
        }
        return canBePlaced
    }

    private fun isPlacementLegal(word: Word, row: Int, column: Int): Boolean {
        val illegal: Boolean = if (word.isVertical) {
            isInterference(row, column + 1, row + 1, column) || isInterference(
                row, column - 1, row + 1, column
            ) || overwritingVerticalWord(row, column) || invadingTerritory(word, row, column)
        } else {
            isInterference(row + 1, column, row, column + 1) || isInterference(
                row - 1, column, row, column + 1
            ) || overwritingHorizontalWord(row, column) || invadingTerritory(word, row, column)
        }
        return !illegal
    }

    private fun isInterference(row: Int, column: Int, nextRow: Int, nextColumn: Int): Boolean {
        return isValidPosition(row, column) && isValidPosition(nextRow, nextColumn) && isLetter(
            row, column
        ) && isLetter(nextRow, nextColumn)
    }

    fun isLetter(row: Int, column: Int): Boolean = grid[row][column] != EMPTY_CELL

    private fun fitsOnGrid(word: Word): Boolean = if (word.isVertical) word.row + word.text.length <= GRID_SIZE
    else word.column + word.text.length <= GRID_SIZE

    private fun overwritingHorizontalWord(row: Int, column: Int): Boolean {
        val leftColumn = column - 1
        return isValidPosition(row, leftColumn) && isLetter(row, column) && isLetter(row, leftColumn)
    }

    private fun overwritingVerticalWord(row: Int, column: Int): Boolean {
        val rowAbove = row - 1
        return isValidPosition(rowAbove, column) && isLetter(row, column) && isLetter(rowAbove, column)
    }

    private fun doesCharacterExist(row: Int, column: Int): Boolean {
        return isValidPosition(row, column) && isLetter(row, column)
    }

    private fun endOfWord(word: Word, row: Int, column: Int): Boolean {
        return if (word.isVertical) word.row + word.text.length - 1 == row
        else word.column + word.text.length - 1 == column
    }

    fun getIntersections(): Int {
        var intersections = 0
        for (row in 0 until GRID_SIZE) {
            for (column in 0 until GRID_SIZE) {
                if (isLetter(row, column)) {
                    ++intersections
                }
            }
        }
        return intersections
    }

    private fun invadingTerritory(word: Word, row: Int, column: Int): Boolean {
        val invading: Boolean
        val empty = !isLetter(row, column)
        invading = if (word.isVertical) {
            val weHaveNeighbors =
                (doesCharacterExist(row, column - 1) || doesCharacterExist(row, column + 1)) || endOfWord(
                    word, row, column
                ) && doesCharacterExist(row + 1, column)
            empty && weHaveNeighbors
        } else {
            val weHaveNeighbors =
                (doesCharacterExist(row - 1, column) || doesCharacterExist(row + 1, column)) || endOfWord(
                    word, row, column
                ) && doesCharacterExist(row, column + 1)
            empty && weHaveNeighbors
        }
        return invading
    }
}

fun generateCrossword(words: List<String>, seed: Int): Array<CharArray> {
    val random = Random(seed)
    val usedWords = mutableListOf<String>()
    val generatedGrids: MutableList<CrosswordGeneratorUtils> = mutableListOf()
    val goodStartingLetters = mutableSetOf<Char>()

    fun isGoodWord(word: String): Boolean {
        var goodWord = false
        for (letter in goodStartingLetters) {
            if (letter == word[0]) {
                goodWord = true
                break
            }
        }
        return goodWord
    }

    fun getUnusedWords(): List<String> {
        return words.filter { !usedWords.contains(it) }
    }

    fun getRandomWord(wordList: List<String>): String {
        return wordList.random(random)
    }

    fun getRandomWord(wordList: List<String>, size: Int): String {
        return wordList.filter { it.length >= size }.random(random)
    }

    fun pushUsedWords(text: String) {
        usedWords.add(text)
        text.toCharArray().filter { goodStartingLetters.add(it) }
    }

    fun getBestGrid(grids: List<CrosswordGeneratorUtils>): CrosswordGeneratorUtils {
        var bestGrid = grids[0]
        for (grid in grids) {
            if (grid.getIntersections() >= bestGrid.getIntersections()) {
                bestGrid = grid
            }
        }
        return bestGrid
    }

    fun getAWordToTry(): String {
        var word = getRandomWord(words)
        var goodWord = isGoodWord(word)

        while (usedWords.contains(word) || !goodWord) {
            word = getRandomWord(words)
            goodWord = isGoodWord(word)
        }
        return word
    }

    fun attemptToPlaceWordOnGrid(grid: CrosswordGeneratorUtils, word: Word): Boolean {
        val text = getAWordToTry()
        for (row in 0 until CrosswordGeneratorUtils.GRID_SIZE) {
            for (column in 0 until CrosswordGeneratorUtils.GRID_SIZE) {
                word.text = text
                word.row = row
                word.column = column
                word.isVertical = random.nextBoolean()

                if (grid.isLetter(row, column)) {
                    if (grid.tryUpdate(word)) {
                        pushUsedWords(word.text)
                        return true
                    }
                }
            }
        }
        return false
    }

    fun generateGrids() {
        generatedGrids.clear()
        for (gridsMade in 0 until CrosswordGeneratorUtils.GRIDS_TO_MAKE) {
            val grid = CrosswordGeneratorUtils()
            val word = Word(getRandomWord(getUnusedWords(), 5), 0, 0, false)
            grid.tryUpdate(word)
            pushUsedWords(word.text)
            var continuousFails = 0
            for (attempts in 0 until CrosswordGeneratorUtils.ATTEMPTS_TO_FIT_WORDS) {
                val placed = attemptToPlaceWordOnGrid(grid, word)
                if (placed) {
                    continuousFails = 0
                } else {
                    continuousFails++
                }
                if (continuousFails > 500) {
                    break
                }
            }
            generatedGrids.add(grid)
            if (grid.getIntersections() >= 25) {
                break
            }
        }
    }
    generateGrids()

    return getBestGrid(generatedGrids).getGrid()
}

fun shuffleWords(input: Array<CharArray>, shuffleIterations: Int, seed: Int): List<List<Char>> {
    val output = input.toList().map { it.toMutableList() }
    val random = Random(seed)
    val lettersInWordsCoords = output.fold(mutableListOf<Pair<Int, Int>>()) { acc, row ->
        for (column in row.indices) {
            if (row[column] != CrosswordGeneratorUtils.EMPTY_CELL) {
                acc.add(Pair(output.indexOf(row), column))
            }
        }
        acc
    }
    lettersInWordsCoords.shuffle(random)
    lettersInWordsCoords.take(shuffleIterations).forEach {
        val (row, column) = it
        val (newRow, newColumn) = lettersInWordsCoords.random(random)
        val temp = output[row][column]
        output[row][column] = output[newRow][newColumn]
        output[newRow][newColumn] = temp
    }
    return output
}

fun main() {
    val words =
        File("/Users/kheynov/Downloads/words.txt").readLines()
//    File("/Users/kheynov/Downloads/words.txt").writeText(words.joinToString("\n"))
    val seed = 15
    var grid = generateCrossword(words, seed)
    for (row in grid) {
        for (cell in row) {
            print("$cell ")
        }
        println()
    }
    println()
    val gridRes = shuffleWords(grid, 10, seed)
    for (row in gridRes) {
        for (cell in row) {
            print("$cell ")
        }
        println()
    }
}