package com.iqlock.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Riddle.kt — Room entity representing a single IQ challenge question.
 *
 * Fields:
 *  - id: auto-generated primary key
 *  - question: the riddle text shown to the user
 *  - answer: the correct answer (matched case-insensitively)
 *  - hint: optional hint shown after 40 seconds
 *  - type: category (LOGIC, PATTERN, SEQUENCE, VISUAL, DEDUCTION)
 *  - difficulty: 1=easy, 2=medium, 3=hard (for future filtering)
 *  - timesShown: how many times this riddle has been presented
 *  - lastShownAt: epoch millis of last presentation (for rotation tracking)
 *  - options: pipe-separated multiple-choice options (empty = open answer)
 *  - correctOptionIndex: 0-based index in options list (-1 = open answer)
 */
@Entity(tableName = "riddles")
data class Riddle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "question")
    val question: String,

    @ColumnInfo(name = "answer")
    val answer: String,

    @ColumnInfo(name = "hint")
    val hint: String = "",

    @ColumnInfo(name = "type")
    val type: String = RiddleType.LOGIC.name,

    @ColumnInfo(name = "difficulty")
    val difficulty: Int = 2,

    @ColumnInfo(name = "times_shown")
    val timesShown: Int = 0,

    @ColumnInfo(name = "last_shown_at")
    val lastShownAt: Long = 0L,

    @ColumnInfo(name = "options")
    val options: String = "",            // pipe-separated: "A|B|C|D"

    @ColumnInfo(name = "correct_option_index")
    val correctOptionIndex: Int = -1     // -1 means open-ended answer
) {
    /** Parsed list of multiple-choice options. Empty list → open-ended. */
    fun optionList(): List<String> =
        if (options.isBlank()) emptyList() else options.split("|")

    /** True when the given input matches the correct answer (case-insensitive, trimmed). */
    fun isCorrect(input: String): Boolean =
        input.trim().equals(answer.trim(), ignoreCase = true)

    /** True when this riddle uses multiple-choice format. */
    fun isMultipleChoice(): Boolean = correctOptionIndex >= 0
}

enum class RiddleType {
    LOGIC, PATTERN, SEQUENCE, VISUAL, DEDUCTION
}
