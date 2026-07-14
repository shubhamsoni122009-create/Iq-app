package com.iqlock.app.engine

import kotlin.math.*

/**
 * RelationshipEngine.kt — Generates the Stage 3 master challenge.
 *
 * Given two answer strings from Stage 1 and Stage 2:
 *  1. Extracts a "numeric essence" from each (direct parse, letter-sum, or hybrid).
 *  2. Evaluates which mathematical/logical relationships apply to that pair.
 *  3. Randomly selects one applicable relationship as the CORRECT answer.
 *  4. Generates 5 highly plausible but verifiably-false distractors from other templates.
 *  5. Returns a [MasterChallenge] with all 6 options shuffled.
 *
 * Design goals:
 *  - Stage 3 is unsolvable without knowing both A1 and A2 — the relationship
 *    depends on the specific numeric pair, not memorizable facts.
 *  - Every unlock attempt produces a DIFFERENT relationship template even for the
 *    same riddle pair, because multiple templates can apply and one is chosen randomly.
 */
object RelationshipEngine {

    // ── Numeric Essence ────────────────────────────────────────────────────────

    /**
     * Convert any answer string to a stable positive integer for relationship analysis.
     *
     * Priority:
     *   1. Direct integer parse → use as-is.
     *   2. Roman numeral → decode.
     *   3. Spelled-out digit words → decode.
     *   4. Fallback → sum of (letter position 1-26) for each letter in the answer.
     */
    fun numericEssence(raw: String): Int {
        val s = raw.trim().uppercase()

        // 1. Direct integer
        s.toIntOrNull()?.let { return abs(it).coerceAtLeast(1) }

        // 2. Roman numerals (supports up to MMMCMXCIX = 3999)
        romanToInt(s)?.let { if (it > 0) return it }

        // 3. Spelled-out English number words
        spelledToInt(s)?.let { if (it > 0) return it }

        // 4. Letter-position sum: A=1, B=2, … Z=26
        val letterSum = s.filter { it.isLetter() }.sumOf { it - 'A' + 1 }
        return letterSum.coerceAtLeast(1)
    }

    private fun romanToInt(s: String): Int? {
        val map = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50,
                        'C' to 100, 'D' to 500, 'M' to 1000)
        if (s.any { it !in map }) return null
        var result = 0
        for (i in s.indices) {
            val cur = map[s[i]]!!
            val next = if (i + 1 < s.length) map[s[i + 1]]!! else 0
            result += if (cur < next) -cur else cur
        }
        return if (result > 0) result else null
    }

    private fun spelledToInt(s: String): Int? = when (s) {
        "ZERO" -> 0; "ONE" -> 1; "TWO" -> 2; "THREE" -> 3; "FOUR" -> 4
        "FIVE" -> 5; "SIX" -> 6; "SEVEN" -> 7; "EIGHT" -> 8; "NINE" -> 9
        "TEN" -> 10; "ELEVEN" -> 11; "TWELVE" -> 12; "THIRTEEN" -> 13
        "FOURTEEN" -> 14; "FIFTEEN" -> 15; "SIXTEEN" -> 16; "SEVENTEEN" -> 17
        "EIGHTEEN" -> 18; "NINETEEN" -> 19; "TWENTY" -> 20
        else -> null
    }

    // ── Math Helpers ───────────────────────────────────────────────────────────

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        if (n == 2) return true
        if (n % 2 == 0) return false
        return (3..sqrt(n.toDouble()).toInt() step 2).none { n % it == 0 }
    }

    private fun isPerfectSquare(n: Int): Boolean {
        val r = sqrt(n.toDouble()).toInt()
        return r * r == n
    }

    private fun isFibonacci(n: Int): Boolean {
        fun isPerfectSq(x: Int) = isPerfectSquare(x)
        return isPerfectSq(5 * n * n + 4) || isPerfectSq(5 * n * n - 4)
    }

    private fun digitSum(n: Int): Int = abs(n).toString().sumOf { it.digitToInt() }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private fun lcm(a: Int, b: Int): Int = abs(a * b) / gcd(a, b)

    private fun isTriangular(n: Int): Boolean {
        // n is triangular if 8n+1 is a perfect square
        return isPerfectSquare(8 * n + 1)
    }

    private fun primeFactors(n: Int): List<Int> {
        var num = abs(n)
        val factors = mutableListOf<Int>()
        var d = 2
        while (d * d <= num) {
            while (num % d == 0) { factors.add(d); num /= d }
            d++
        }
        if (num > 1) factors.add(num)
        return factors
    }

    private fun alphabetPosition(answer: String): Int =
        answer.trim().uppercase().firstOrNull()?.let { it - 'A' + 1 } ?: 1

    // ── Distractor Pool ────────────────────────────────────────────────────────

    /**
     * Generate 5 plausible but FALSE relationship descriptions.
     * We cycle through all templates, skip the correct one, and collect the rest.
     */
    private fun generateDistractors(
        a: Int, b: Int,
        correctKey: String,
        all: List<RelTemplate>
    ): List<String> {
        val false_ = mutableListOf<String>()
        // Templates whose condition is FALSE for (a,b) make great distractors
        for (t in all.shuffled()) {
            if (t.key == correctKey) continue
            if (!t.condition(a, b)) {
                // Verify the description would be wrong (it's always wrong if condition is false)
                false_.add(t.describeAs(a, b))
            }
            if (false_.size == 5) break
        }
        // If we couldn't find 5 purely-false ones, fill with numeric-junk options
        while (false_.size < 5) {
            val junk = junkOption(a, b, correctKey, false_)
            false_.add(junk)
        }
        return false_.take(5)
    }

    private fun junkOption(a: Int, b: Int, correctKey: String, existing: List<String>): String {
        val options = listOf(
            "Their sum (${ a + b }) is both a perfect square and a prime",
            "Both values appear in row 7 of Pascal's triangle",
            "The ratio $a:$b simplifies to the golden ratio (1.618…)",
            "${a} reversed equals ${b}",
            "Both are perfect cubes",
            "Their LCM equals their sum",
            "Both share the same prime factorization structure",
            "Their product is a Fibonacci number",
            "One is the cube root of the other",
            "Both values are divisors of 360"
        )
        return options.firstOrNull { it !in existing } ?: "Both values encode the same Roman numeral"
    }

    // ── Relationship Templates ─────────────────────────────────────────────────

    private data class RelTemplate(
        val key: String,
        val condition: (Int, Int) -> Boolean,
        val describe: (Int, Int) -> String,   // The TRUE description of the relationship
        val explain: (Int, Int) -> String      // Post-solve explanation
    ) {
        fun describeAs(a: Int, b: Int): String = describe(a, b)
    }

    private val templates: List<RelTemplate> = listOf(

        RelTemplate(
            key = "BOTH_PRIME",
            condition = { a, b -> isPrime(a) && isPrime(b) },
            describe = { a, b -> "Both $a and $b are prime numbers — indivisible by anything other than 1 and themselves" },
            explain = { a, b -> "$a is prime (factors: 1, $a) and $b is prime (factors: 1, $b). Twin primes share this indivisibility property." }
        ),

        RelTemplate(
            key = "BOTH_PERFECT_SQUARE",
            condition = { a, b -> isPerfectSquare(a) && isPerfectSquare(b) },
            describe = { a, b -> "Both $a and $b are perfect squares — each is the product of an integer multiplied by itself" },
            explain = { a, b -> "√$a = ${sqrt(a.toDouble()).toInt()} and √$b = ${sqrt(b.toDouble()).toInt()}." }
        ),

        RelTemplate(
            key = "BOTH_FIBONACCI",
            condition = { a, b -> isFibonacci(a) && isFibonacci(b) },
            describe = { a, b -> "Both $a and $b are members of the Fibonacci sequence (1,1,2,3,5,8,13,21…)" },
            explain = { a, b -> "The Fibonacci sequence is built by adding consecutive terms. Both $a and $b appear in this infinite series." }
        ),

        RelTemplate(
            key = "SUM_PRIME",
            condition = { a, b -> isPrime(a + b) },
            describe = { a, b -> "The sum of $a and $b equals ${a + b}, which is a prime number" },
            explain = { a, b -> "$a + $b = ${a + b}. This sum has no divisors other than 1 and itself." }
        ),

        RelTemplate(
            key = "PRODUCT_PERFECT_SQUARE",
            condition = { a, b -> isPerfectSquare(a * b) },
            describe = { a, b -> "The product $a × $b = ${a * b} is a perfect square (√${a * b} = ${sqrt((a * b).toDouble()).toInt()})" },
            explain = { a, b -> "When $a and $b are multiplied together, the result ${a * b} is a perfect square." }
        ),

        RelTemplate(
            key = "SAME_DIGIT_SUM",
            condition = { a, b -> digitSum(a) == digitSum(b) },
            describe = { a, b -> "Both $a and $b reduce to the same digit sum (${digitSum(a)}) — their digits sum to the same number" },
            explain = { a, b -> "Digit sum of $a: ${abs(a).toString().map { it.digitToInt() }.joinToString("+")} = ${digitSum(a)}. Digit sum of $b: ${abs(b).toString().map { it.digitToInt() }.joinToString("+")} = ${digitSum(b)}." }
        ),

        RelTemplate(
            key = "ONE_IS_DOUBLE",
            condition = { a, b -> a == 2 * b || b == 2 * a },
            describe = { a, b ->
                if (b == 2 * a) "$b is exactly twice $a — a 1:2 doubling relationship"
                else "$a is exactly twice $b — a 1:2 doubling relationship"
            },
            explain = { a, b ->
                if (b == 2 * a) "$a × 2 = $b. One answer is the double of the other."
                else "$b × 2 = $a. One answer is the double of the other."
            }
        ),

        RelTemplate(
            key = "CONSECUTIVE",
            condition = { a, b -> abs(a - b) == 1 },
            describe = { a, b -> "$a and $b are consecutive integers — they differ by exactly 1" },
            explain = { a, b -> "|$a - $b| = 1. They are adjacent on the number line." }
        ),

        RelTemplate(
            key = "SAME_PARITY",
            condition = { a, b -> (a % 2) == (b % 2) },
            describe = { a, b ->
                if (a % 2 == 0) "Both $a and $b are even numbers — both divisible by 2"
                else "Both $a and $b are odd numbers — neither is divisible by 2"
            },
            explain = { a, b ->
                if (a % 2 == 0) "$a ÷ 2 = ${a / 2} (no remainder), $b ÷ 2 = ${b / 2} (no remainder)."
                else "$a and $b both leave remainder 1 when divided by 2."
            }
        ),

        RelTemplate(
            key = "GCD_GT_1",
            condition = { a, b -> gcd(a, b) > 1 },
            describe = { a, b -> "$a and $b share a common factor of ${gcd(a, b)} — they are not coprime" },
            explain = { a, b -> "gcd($a, $b) = ${gcd(a, b)}. Both numbers are divisible by ${gcd(a, b)}." }
        ),

        RelTemplate(
            key = "COPRIME",
            condition = { a, b -> gcd(a, b) == 1 },
            describe = { a, b -> "$a and $b are coprime — their only common factor is 1" },
            explain = { a, b -> "gcd($a, $b) = 1. No integer > 1 divides both $a and $b." }
        ),

        RelTemplate(
            key = "PYTHAGOREAN_PAIR",
            condition = { a, b ->
                val sq = a * a + b * b
                isPerfectSquare(sq)
            },
            describe = { a, b ->
                val hyp = sqrt((a * a + b * b).toDouble()).toInt()
                "$a and $b are the two legs of a right triangle with hypotenuse $hyp (a Pythagorean pair: $a² + $b² = $hyp²)"
            },
            explain = { a, b ->
                val hyp = sqrt((a * a + b * b).toDouble()).toInt()
                "$a² + $b² = ${a * a} + ${b * b} = ${a * a + b * b} = $hyp²."
            }
        ),

        RelTemplate(
            key = "SQUARE_OF_OTHER",
            condition = { a, b ->
                val sa = sqrt(a.toDouble()).toInt()
                val sb = sqrt(b.toDouble()).toInt()
                (sa * sa == a && sa == b) || (sb * sb == b && sb == a)
            },
            describe = { a, b ->
                if (isPerfectSquare(a) && sqrt(a.toDouble()).toInt() == b)
                    "$a is the square of $b ($b² = $a)"
                else
                    "$b is the square of $a ($a² = $b)"
            },
            explain = { a, b ->
                if (isPerfectSquare(a) && sqrt(a.toDouble()).toInt() == b)
                    "$b × $b = $a."
                else
                    "$a × $a = $b."
            }
        ),

        RelTemplate(
            key = "BOTH_TRIANGULAR",
            condition = { a, b -> isTriangular(a) && isTriangular(b) },
            describe = { a, b -> "Both $a and $b are triangular numbers — they represent the total of dots in an equilateral triangle arrangement" },
            explain = { a, b -> "Triangular numbers: 1,3,6,10,15,21,28,36,45,55,66,78,91,105… Both $a and $b appear in this sequence." }
        ),

        RelTemplate(
            key = "SUM_PERFECT_SQUARE",
            condition = { a, b -> isPerfectSquare(a + b) },
            describe = { a, b -> "The sum $a + $b = ${a + b} is a perfect square (√${a + b} = ${sqrt((a + b).toDouble()).toInt()})" },
            explain = { a, b -> "Adding both answers gives ${a + b}, a perfect square." }
        ),

        RelTemplate(
            key = "DIFF_PRIME",
            condition = { a, b -> isPrime(abs(a - b)) },
            describe = { a, b -> "The absolute difference |$a − $b| = ${abs(a - b)} is a prime number" },
            explain = { a, b -> "|$a - $b| = ${abs(a - b)}, which is prime." }
        ),

        RelTemplate(
            key = "SHARED_PRIME_FACTOR",
            condition = { a, b ->
                val fa = primeFactors(a).toSet()
                val fb = primeFactors(b).toSet()
                fa.intersect(fb).isNotEmpty()
            },
            describe = { a, b ->
                val shared = primeFactors(a).toSet().intersect(primeFactors(b).toSet())
                "$a and $b share the prime factor ${shared.min()} in their prime factorizations"
            },
            explain = { a, b ->
                val fa = primeFactors(a)
                val fb = primeFactors(b)
                val shared = fa.toSet().intersect(fb.toSet())
                "$a = ${fa.joinToString("×")}; $b = ${fb.joinToString("×")}. Shared: ${shared.joinToString(", ")}."
            }
        ),

        RelTemplate(
            key = "REVERSAL",
            condition = { a, b ->
                val sa = a.toString()
                val sb = b.toString()
                sa.length == sb.length && sa == sb.reversed() && sa != sb
            },
            describe = { a, b -> "$a reversed gives $b — the digits of one answer are the mirror image of the other" },
            explain = { a, b -> "${a.toString().reversed()} = $b. Reading $a backwards gives exactly $b." }
        ),

        RelTemplate(
            key = "LCM_IS_PRODUCT",
            condition = { a, b -> lcm(a, b) == a * b },
            describe = { a, b -> "$a and $b are coprime — their LCM (${ lcm(a, b) }) equals their product ($a × $b = ${a * b})" },
            explain = { a, b -> "When gcd(a,b) = 1, lcm(a,b) = a×b. Here gcd($a,$b) = 1, so lcm = ${a * b}." }
        ),

        RelTemplate(
            key = "BOTH_ODD_PRIME",
            condition = { a, b -> isPrime(a) && isPrime(b) && a != 2 && b != 2 },
            describe = { a, b -> "Both $a and $b are odd primes — prime numbers greater than 2 are always odd, and both qualify" },
            explain = { a, b -> "2 is the only even prime. $a and $b are both prime and both odd." }
        ),

        RelTemplate(
            key = "SAME_LETTER_VALUE",
            condition = { a, b -> a == b },
            describe = { a, b -> "Both answers encode the same numerical value ($a) — despite looking different, they represent the same quantity" },
            explain = { a, b -> "After extracting the numeric essence of each answer, both yield $a." }
        ),

        RelTemplate(
            key = "SUM_FIBONACCI",
            condition = { a, b -> isFibonacci(a + b) },
            describe = { a, b -> "The sum of both answers ($a + $b = ${a + b}) lands on a Fibonacci number" },
            explain = { a, b -> "${a + b} appears in the Fibonacci sequence: …${a + b - (if (a+b > 1) (a+b)/2 else 1)}, ${a + b},…" }
        ),

        RelTemplate(
            key = "ONE_DIVIDES_OTHER",
            condition = { a, b -> (a != b) && (a % b == 0 || b % a == 0) },
            describe = { a, b ->
                if (a % b == 0) "$b divides $a perfectly — $a = $b × ${a / b} with no remainder"
                else "$a divides $b perfectly — $b = $a × ${b / a} with no remainder"
            },
            explain = { a, b ->
                if (a % b == 0) "$a ÷ $b = ${a / b} with remainder 0."
                else "$b ÷ $a = ${b / a} with remainder 0."
            }
        )
    )

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Generate a [MasterChallenge] for the given pair of answers.
     *
     * @param answer1 The user's correct answer to Stage 1.
     * @param answer2 The user's correct answer to Stage 2.
     * @param riddle1Question The Stage 1 question text (for framing the master question).
     * @param riddle2Question The Stage 2 question text (for framing the master question).
     */
    fun generate(
        answer1: String,
        answer2: String,
        riddle1Question: String,
        riddle2Question: String
    ): MasterChallenge {
        val a = numericEssence(answer1)
        val b = numericEssence(answer2)

        // Collect all applicable templates (condition is TRUE for this pair)
        val applicable = templates.filter { it.condition(a, b) }.shuffled()

        val chosen = applicable.firstOrNull() ?: templates
            .filter { !it.condition(a, b) }   // fallback: pick any template
            .shuffled()
            .first()

        // Generate 5 false distractors
        val distractors = generateDistractors(a, b, chosen.key, templates)

        // Build correct description
        val correctDesc = chosen.describeAs(a, b)
        val explanation = chosen.explain(a, b)

        // Shuffle correct + distractors together
        val allOptions = (listOf(correctDesc) + distractors).shuffled()
        val correctIdx = allOptions.indexOf(correctDesc)

        val question = buildMasterQuestion(answer1, answer2, riddle1Question, riddle2Question)

        return MasterChallenge(
            question = question,
            options = allOptions,
            correctIndex = correctIdx,
            relationshipKey = chosen.key,
            explanation = explanation
        )
    }

    private fun buildMasterQuestion(
        a1: String, a2: String,
        q1: String, q2: String
    ): String {
        val intro = listOf(
            "Your previous answers unlocked a hidden connection. Identify the relationship:",
            "A master puzzle emerges from your two answers. Discover the hidden link:",
            "Both answers share a precise logical property. Which of these describes it?",
            "The two answers you discovered are connected by a single unifying rule. Find it:",
            "A single mathematical or logical truth connects your two answers. What is it?"
        ).random()

        return "$intro\n\nYour Answer to Challenge I: \"$a1\"\nYour Answer to Challenge II: \"$a2\"\n\nWhat hidden relationship do these two answers share?"
    }
}
