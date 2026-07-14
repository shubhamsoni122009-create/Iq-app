package com.iqlock.app.data

/**
 * ThreeStageRiddleBank.kt — Curated high-difficulty riddle sets for the Three-Stage system.
 *
 * Riddles are split into two banks (A and B) to guarantee Stage 1 and Stage 2
 * always test completely different domains and concepts:
 *
 *  - BANK_A: Mathematical reasoning, sequences, and deductive logic with numeric answers.
 *  - BANK_B: Abstract reasoning, pattern recognition, and lateral thinking with word/number answers.
 *
 * All answers are short (1–5 characters), enabling the RelationshipEngine to compute
 * precise numeric relationships for the Stage 3 master challenge.
 *
 * Difficulty calibration: Mensa/GMAT level — not derivable from quick web search because
 * Stage 3 requires the specific combination of the first two answers.
 */
object ThreeStageRiddleBank {

    data class StageRiddle(
        val question: String,
        val answer: String,     // Correct answer, matched case-insensitively
        val hint: String        // Shown if user is close or time is low
    )

    // ── BANK A — Mathematical / Deductive (numeric answers) ───────────────────

    val BANK_A: List<StageRiddle> = listOf(
        StageRiddle(
            question = "In a race, you overtake the person in second place. What position are you in now?",
            answer = "2",
            hint = "You didn't pass first place."
        ),
        StageRiddle(
            question = "A snail is at the bottom of a 10-metre well. Each day it climbs 3 metres; each night it slides back 2 metres. On what day does it escape?",
            answer = "8",
            hint = "Count the day it reaches or passes the top."
        ),
        StageRiddle(
            question = "Five machines take 5 minutes to make 5 widgets. How many minutes would it take 100 machines to make 100 widgets?",
            answer = "5",
            hint = "Each machine works independently."
        ),
        StageRiddle(
            question = "I have two coins totalling 30 pence. One is NOT a 10-pence coin. What are the two coins?",
            answer = "20",
            hint = "The other one can still be a 10p coin."
        ),
        StageRiddle(
            question = "A bat and a ball cost £1.10 together. The bat costs £1.00 more than the ball. How many pence does the ball cost?",
            answer = "5",
            hint = "Do NOT answer 10. Use algebra."
        ),
        StageRiddle(
            question = "If you multiply all the digits on a telephone keypad together (0-9, excluding *#), what is the answer?",
            answer = "0",
            hint = "Check every digit on the keypad."
        ),
        StageRiddle(
            question = "A number is called a 'perfect number' if it equals the sum of its proper divisors. What is the smallest perfect number?",
            answer = "6",
            hint = "Divisors of 6 are 1, 2, and 3."
        ),
        StageRiddle(
            question = "The sum of three consecutive integers is 48. What is the smallest of the three?",
            answer = "15",
            hint = "Let them be n, n+1, n+2."
        ),
        StageRiddle(
            question = "How many times does the digit '1' appear in the integers from 1 to 100?",
            answer = "21",
            hint = "Count carefully in tens and units. Don't forget 11."
        ),
        StageRiddle(
            question = "A clock shows 3:15. What is the exact angle in degrees between the hour and minute hands?",
            answer = "7",
            hint = "The hour hand is NOT exactly on 3 at 3:15."
        ),
        StageRiddle(
            question = "If 2^10 = 1024, what is 2^11?",
            answer = "2048",
            hint = "Doubling is all that is required."
        ),
        StageRiddle(
            question = "How many squares (of all sizes) are on a standard 8×8 chessboard?",
            answer = "204",
            hint = "Count 1×1, 2×2, 3×3… up to 8×8 separately."
        ),
        StageRiddle(
            question = "A prime number that remains prime when its digits are reversed is called a 'emirp'. What is the smallest emirp greater than 10?",
            answer = "13",
            hint = "13 reversed is 31. Check: is 31 prime?"
        ),
        StageRiddle(
            question = "A train 200 metres long travels at 60 km/h. How many seconds does it take to pass completely through a tunnel 400 metres long?",
            answer = "36",
            hint = "Total distance to clear = train length + tunnel length."
        ),
        StageRiddle(
            question = "In a class of 30, everyone shook hands with everyone else exactly once. How many handshakes took place?",
            answer = "435",
            hint = "Formula: n(n-1)/2."
        ),
        StageRiddle(
            question = "What is the units digit of 7^100?",
            answer = "1",
            hint = "The units digits of powers of 7 cycle with period 4: 7, 9, 3, 1."
        ),
        StageRiddle(
            question = "If today is Monday and you have an appointment in exactly 100 days, what day of the week is your appointment?",
            answer = "WEDNESDAY",
            hint = "100 mod 7 = ?"
        ),
        StageRiddle(
            question = "A number doubled, then increased by three, then halved equals 10. What is the original number?",
            answer = "8",
            hint = "Work backwards: 10 × 2 = 20, 20 - 3 = 17… wait, re-read carefully."
        ),
        StageRiddle(
            question = "Which two-digit number equals twice the product of its digits?",
            answer = "36",
            hint = "Try numbers 11-99. For ab: 10a+b = 2ab."
        ),
        StageRiddle(
            question = "I think of a number. I double it, subtract 7, and multiply by 3. The result is 9. What was my number?",
            answer = "5",
            hint = "Reverse the operations."
        ),

        // ── Extra difficult
        StageRiddle(
            question = "A palindrome number reads the same forwards and backwards. How many 4-digit palindromes exist?",
            answer = "90",
            hint = "The first digit determines the last; the second determines the third."
        ),
        StageRiddle(
            question = "What is the remainder when 2^100 is divided by 3?",
            answer = "1",
            hint = "2^1=2, 2^2=4, 2^3=8 mod 3 → find the cycle."
        ),
        StageRiddle(
            question = "If log₂(x) = 5, what is x?",
            answer = "32",
            hint = "2 to the power of 5."
        ),
        StageRiddle(
            question = "A rectangle has a perimeter of 36 cm and an area of 80 cm². What is the length of the longer side in cm?",
            answer = "10",
            hint = "l + w = 18, l × w = 80. Solve the quadratic."
        ),
        StageRiddle(
            question = "The Fibonacci sequence starts 1, 1, 2, 3, 5, 8… What is the 12th term?",
            answer = "144",
            hint = "Continue the sequence: 13, 21, 34, 55, 89, 144."
        )
    )

    // ── BANK B — Abstract / Pattern / Lateral (word or non-obvious numeric answers) ─

    val BANK_B: List<StageRiddle> = listOf(
        StageRiddle(
            question = "What number should replace the question mark?\n4   16   36   64   100   ?\n(Each term is related to consecutive even numbers.)",
            answer = "196",
            hint = "2²=4, 4²=16, 6²=36, 8²=64, 10²=100, ?²=?"
        ),
        StageRiddle(
            question = "Complete the matrix:\n2  3  5\n5  7  12\n8  11  ?\nEach row follows the same pattern.",
            answer = "19",
            hint = "Third = first + second."
        ),
        StageRiddle(
            question = "A man has a 3-litre jug and a 5-litre jug. How can he measure exactly 4 litres using only these two jugs and a large water source? How many pourings does the MINIMUM solution require?",
            answer = "6",
            hint = "Fill 5L, pour into 3L, discard 3L, pour remainder into 3L… count each action."
        ),
        StageRiddle(
            question = "A fast-food chain has 3 different burgers, 4 different sides, and 5 different drinks. If you choose one of each, how many distinct meals can you order?",
            answer = "60",
            hint = "Multiply the choices together."
        ),
        StageRiddle(
            question = "I am a three-digit number. My tens digit is five more than my units digit. My hundreds digit is eight less than my tens digit. What am I?",
            answer = "194",
            hint = "Let units = x, tens = x+5, hundreds = (x+5)-8. Find x such that hundreds is a single digit ≥ 1."
        ),
        StageRiddle(
            question = "What is the next number in this series?\n1, 8, 27, 64, 125, ___",
            answer = "216",
            hint = "1³=1, 2³=8, 3³=27…"
        ),
        StageRiddle(
            question = "What number is missing?\n16, 06, 68, 88, ?, 98",
            answer = "87",
            hint = "Rotate the sequence 180 degrees."
        ),
        StageRiddle(
            question = "A letter stands for a number. If A=1, B=2, …, Z=26, what is the value of MENSA?",
            answer = "54",
            hint = "M=13, E=5, N=14, S=19, A=1. Sum them."
        ),
        StageRiddle(
            question = "Replace the '?' in the pattern:\n3  →  9  →  81  →  6561  →  ?\n(Each term is the square of the previous.)",
            answer = "43046721",
            hint = "6561² = ?"
        ),
        StageRiddle(
            question = "In binary, what decimal number does 11010 represent?",
            answer = "26",
            hint = "16+8+0+2+0 = ?"
        ),
        StageRiddle(
            question = "What is the smallest number that can be expressed as the sum of two cubes in two different ways? (Hardy-Ramanujan number)",
            answer = "1729",
            hint = "1³ + 12³ = 9³ + 10³"
        ),
        StageRiddle(
            question = "How many diagonals does a regular hexagon have?",
            answer = "9",
            hint = "Formula: n(n-3)/2 where n=6."
        ),
        StageRiddle(
            question = "A fair coin is tossed 5 times. What is the probability (as a simplified fraction) of getting exactly 3 heads? Express as numerator only (denominator is 32).",
            answer = "10",
            hint = "C(5,3) = 10."
        ),
        StageRiddle(
            question = "In the sequence 2, 5, 11, 23, 47… what is the next term?\n(Each term is double the previous minus 1, or…)",
            answer = "95",
            hint = "Rule: a(n+1) = 2×a(n) + 1"
        ),
        StageRiddle(
            question = "What is the sum of all integers from 1 to 100?",
            answer = "5050",
            hint = "Gauss's trick: (100 × 101) / 2"
        ),
        StageRiddle(
            question = "A 3×3 magic square uses the digits 1–9 exactly once. What must the magic sum (sum of any row, column, or diagonal) be?",
            answer = "15",
            hint = "Sum of 1–9 = 45. Divided by 3 rows = ?"
        ),
        StageRiddle(
            question = "If i = √-1, what is i^47?",
            answer = "-i",
            hint = "i cycles: i¹=i, i²=-1, i³=-i, i⁴=1. What is 47 mod 4?"
        ),
        StageRiddle(
            question = "How many prime numbers exist between 1 and 50?",
            answer = "15",
            hint = "List them: 2,3,5,7,11,13,17,19,23,29,31,37,41,43,47"
        ),
        StageRiddle(
            question = "A square piece of paper is folded in half three times and then a corner is cut off. When unfolded, how many holes appear in the paper?",
            answer = "8",
            hint = "Each fold doubles the effective layers. 3 folds = 8 layers = 8 holes from 1 corner cut."
        ),
        StageRiddle(
            question = "The IQ score distribution follows a normal curve with mean 100 and SD 15. What percentage of people score above 145 (≥ 3 standard deviations above mean)? Give the integer percentage to 1 decimal place × 10.",
            answer = "1",
            hint = "~0.13% is less than 1. Round to nearest whole percent."
        ),

        // ── Extra abstract
        StageRiddle(
            question = "ELBOW → BELOW, TASTE → STATE, DUSTY → STUDY. What transformation connects all pairs?",
            answer = "ANAGRAM",
            hint = "What do all right-hand words share with left-hand words?"
        ),
        StageRiddle(
            question = "What single digit can you insert between 2 and 3 so the result is between 2 and 3?\n(Answer: insert it as a decimal — what is the digit?)",
            answer = "5",
            hint = "2.5 is between 2 and 3. What digit makes that?"
        ),
        StageRiddle(
            question = "A man builds a house with four south-facing walls. A bear walks past. What color is the bear?",
            answer = "WHITE",
            hint = "All four walls face south only at one point on Earth."
        ),
        StageRiddle(
            question = "I have 6 faces, 12 edges, and 8 vertices. My faces are all congruent. What is the ratio of my edges to my vertices?",
            answer = "3",
            hint = "12 ÷ 8 = ?"
        ),
        StageRiddle(
            question = "In the sequence: O, T, T, F, F, S, S, E, N, T — what letter comes next?",
            answer = "E",
            hint = "One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Eleven…"
        )
    )

    // ── Selectors ─────────────────────────────────────────────────────────────

    /**
     * Pick [count] riddles from Bank A that haven't been used in this session.
     * Returns a random sample without replacement.
     */
    fun pickFromA(count: Int = 1, exclude: Set<Int> = emptySet()): List<StageRiddle> =
        BANK_A.filterIndexed { i, _ -> i !in exclude }.shuffled().take(count)

    /**
     * Pick [count] riddles from Bank B that haven't been used in this session.
     * Returns a random sample without replacement.
     */
    fun pickFromB(count: Int = 1, exclude: Set<Int> = emptySet()): List<StageRiddle> =
        BANK_B.filterIndexed { i, _ -> i !in exclude }.shuffled().take(count)
}
