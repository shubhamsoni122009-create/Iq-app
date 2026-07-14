package com.iqlock.app.engine

/**
 * MasterChallenge.kt — Represents the dynamically-generated Stage 3 puzzle.
 *
 * The puzzle is computed from the numeric essence of both Stage 1 and Stage 2 answers.
 * Only one of the six [options] is correct; [correctIndex] identifies it (0-based).
 * [relationshipKey] is a debug-friendly label for the relationship type used.
 */
data class MasterChallenge(
    /** The master question shown on screen (references both prior answers explicitly). */
    val question: String,

    /** Exactly 6 options — 1 correct, 5 plausible-but-false. Already shuffled. */
    val options: List<String>,

    /** 0-based index of the correct option in [options]. */
    val correctIndex: Int,

    /** Human-readable key of the relationship type, e.g. "PRIME_PAIR". Debug only. */
    val relationshipKey: String,

    /** Short explanation shown after the user solves/fails — reveals the logic. */
    val explanation: String
)
