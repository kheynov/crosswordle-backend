package ru.kheynov.crosswordle

import java.io.File

class WordsStore(
    file: File
) {
    val words = file.readLines()
}