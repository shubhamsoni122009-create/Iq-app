package com.iqlock.app

import com.iqlock.app.data.entity.Riddle
import com.iqlock.app.data.entity.RiddleType

/**
 * RiddleData.kt — Hardcoded dataset of 100 unique IQ challenge riddles.
 *
 * Each riddle has:
 *  - question: shown to the user
 *  - answer: exact text checked by Riddle.isCorrect() (case-insensitive)
 *  - hint: shown after 40 seconds if hints are enabled
 *  - type: LOGIC | PATTERN | SEQUENCE | VISUAL | DEDUCTION
 *  - difficulty: 1=Easy  2=Medium  3=Hard
 *  - options: pipe-separated multiple-choice options ("A|B|C|D"), empty = open-ended
 *  - correctOptionIndex: 0-based index into options (-1 for open-ended)
 *
 * Categories: 20 riddles each × 5 types = 100 total.
 * The answer string MUST match one of the options exactly (if multiple choice)
 * because LockScreenActivity passes the option text directly to Riddle.isCorrect().
 */
object RiddleData {

    fun all(): List<Riddle> = LOGIC + PATTERN + SEQUENCE + VISUAL + DEDUCTION

    // ═══════════════════════════════════════════════════════════
    //  LOGIC  (riddles 1–20)
    // ═══════════════════════════════════════════════════════════
    private val LOGIC = listOf(

        Riddle(question = "A bat and a ball together cost \$1.10. The bat costs exactly \$1.00 more than the ball. How much does the ball cost?",
            answer = "5 cents", hint = "Don't let your intuition fool you — set up the equation.",
            type = RiddleType.LOGIC.name, difficulty = 2,
            options = "10 cents|5 cents|15 cents|1 cent", correctOptionIndex = 1),

        Riddle(question = "In a lake, a patch of lily pads doubles in size every day. If it takes 48 days for the patch to cover the entire lake, how long does it take to cover half the lake?",
            answer = "47 days", hint = "Work backwards from day 48.",
            type = RiddleType.LOGIC.name, difficulty = 2,
            options = "24 days|36 days|47 days|46 days", correctOptionIndex = 2),

        Riddle(question = "It takes 5 machines exactly 5 minutes to make 5 widgets. How long does it take 100 machines to make 100 widgets?",
            answer = "5 minutes", hint = "Each machine works independently.",
            type = RiddleType.LOGIC.name, difficulty = 2,
            options = "100 minutes|5 minutes|10 minutes|1 minute", correctOptionIndex = 1),

        Riddle(question = "A farmer has 17 sheep. All but 9 die in a storm. How many sheep does he have left?",
            answer = "9", hint = "'All but 9' means 9 are the exception.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "8|9|17|0", correctOptionIndex = 1),

        Riddle(question = "You are running a race. You overtake the person in 2nd place. What place are you in now?",
            answer = "2nd", hint = "You cannot overtake 1st by passing 2nd.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "1st|2nd|3rd|Last", correctOptionIndex = 1),

        Riddle(question = "A doctor gives you 3 pills and says to take one every 30 minutes. How long before all the pills are gone?",
            answer = "1 hour", hint = "Take pill 1 at 0 min, pill 2 at 30 min, pill 3 at 60 min.",
            type = RiddleType.LOGIC.name, difficulty = 2,
            options = "90 minutes|1 hour|30 minutes|45 minutes", correctOptionIndex = 1),

        Riddle(question = "Mary's father has five daughters: Nana, Nene, Nini, and Nono. What is the fifth daughter's name?",
            answer = "Mary", hint = "The question itself contains the answer.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "Nunu|Nene|Mary|Nana", correctOptionIndex = 2),

        Riddle(question = "A rooster lays an egg on the very peak of a barn roof. Which way does the egg roll?",
            answer = "Roosters don't lay eggs", hint = "What type of animal lays eggs?",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "North|South|It doesn't roll|Roosters don't lay eggs", correctOptionIndex = 3),

        Riddle(question = "You have two coins that total 30 cents. One of them is NOT a nickel. What are the two coins?",
            answer = "Quarter and nickel", hint = "Only ONE of them is not a nickel.",
            type = RiddleType.LOGIC.name, difficulty = 2,
            options = "Two dimes|Quarter and nickel|Three dimes|Two quarters", correctOptionIndex = 1),

        Riddle(question = "How many times can you subtract the number 5 from 25?",
            answer = "Once", hint = "After the first subtraction, the number is no longer 25.",
            type = RiddleType.LOGIC.name, difficulty = 2,
            options = "5 times|Once|Infinite|Zero", correctOptionIndex = 1),

        Riddle(question = "What is heavier: a pound of gold or a pound of feathers?",
            answer = "Same weight", hint = "Both are measured in pounds.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "Gold|Feathers|Same weight|Depends", correctOptionIndex = 2),

        Riddle(question = "A man builds a house with all four walls facing south. A bear walks by. What color is the bear?",
            answer = "White", hint = "Where on Earth can all four walls of a house face south?",
            type = RiddleType.LOGIC.name, difficulty = 2,
            options = "Brown|Black|White|Orange", correctOptionIndex = 2),

        Riddle(question = "How many months have exactly 28 days?",
            answer = "All 12", hint = "Every month has at least 28 days.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "1|6|12|All 12", correctOptionIndex = 3),

        Riddle(question = "You are alone in a room with an oil lamp, a fireplace, and a candle. You have one match. Which do you light first?",
            answer = "The match", hint = "You need the match to light anything else.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "Oil lamp|Fireplace|Candle|The match", correctOptionIndex = 3),

        Riddle(question = "A man is looking at a photograph. He says: 'Brothers and sisters I have none, but that man's father is my father's son.' Who is in the photograph?",
            answer = "His son", hint = "'My father's son' with no siblings = me. 'That man's father = me' → that man is my son.",
            type = RiddleType.LOGIC.name, difficulty = 3,
            options = "Himself|His brother|His son|His father", correctOptionIndex = 2),

        Riddle(question = "What has cities but no houses, forests but no trees, and water but no fish?",
            answer = "A map", hint = "It shows representations, not real things.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "A dream|A painting|A map|A book", correctOptionIndex = 2),

        Riddle(question = "What gets wetter and wetter the more it dries?",
            answer = "A towel", hint = "Think about what you use after a shower.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "A sponge|A towel|A cloud|A fish", correctOptionIndex = 1),

        Riddle(question = "The more you take, the more you leave behind. What am I?",
            answer = "Footsteps", hint = "Think about physical movement.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "Money|Memories|Footsteps|Time", correctOptionIndex = 2),

        Riddle(question = "What can travel around the world while staying in a corner?",
            answer = "A stamp", hint = "It always sits in the corner of an envelope.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "A letter|A package|A stamp|A phone", correctOptionIndex = 2),

        Riddle(question = "What has hands but cannot clap?",
            answer = "A clock", hint = "Think about a common timekeeping device.",
            type = RiddleType.LOGIC.name, difficulty = 1,
            options = "A mannequin|A tree|A clock|A puppet", correctOptionIndex = 2)
    )

    // ═══════════════════════════════════════════════════════════
    //  PATTERN  (riddles 21–40)
    // ═══════════════════════════════════════════════════════════
    private val PATTERN = listOf(

        Riddle(question = "What letter comes next in this sequence?\nO, T, T, F, F, S, S, E, N, T, _",
            answer = "E", hint = "Say the counting numbers out loud.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "L|E|W|O", correctOptionIndex = 1),

        Riddle(question = "What letter comes next?\nJ, F, M, A, M, J, J, A, S, O, N, _",
            answer = "D", hint = "Think about the calendar.",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "P|D|W|X", correctOptionIndex = 1),

        Riddle(question = "Which number does NOT belong in this group?\n2, 3, 5, 7, 11, 14, 17",
            answer = "14", hint = "All others share a special mathematical property.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "2|5|14|17", correctOptionIndex = 2),

        Riddle(question = "What comes next in the sequence?\nAZ, BY, CX, DW, _",
            answer = "EV", hint = "One sequence moves forward, the other backward through the alphabet.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "EV|FU|EW|DX", correctOptionIndex = 0),

        Riddle(question = "What comes next?\n1, 11, 21, 1211, 111221, _",
            answer = "312211", hint = "Read each term aloud to generate the next one.",
            type = RiddleType.PATTERN.name, difficulty = 3,
            options = "312211|111221|11121|111213", correctOptionIndex = 0),

        Riddle(question = "What letter comes next in this sequence of consonants?\nB, C, D, F, G, H, J, _",
            answer = "K", hint = "Only consonants — skip all vowels.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "K|L|I|E", correctOptionIndex = 0),

        Riddle(question = "Complete the analogy:\nSPRING is to AUTUMN as MORNING is to ___",
            answer = "Evening", hint = "Both pairs are opposites in a daily/yearly cycle.",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "Night|Noon|Evening|Afternoon", correctOptionIndex = 2),

        Riddle(question = "All BLOOPS are RAZZIES. All RAZZIES are LAZZIES.\nAre all BLOOPS definitely LAZZIES?",
            answer = "Yes", hint = "Apply the transitive property of logic.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "Yes|No|Cannot determine|Sometimes", correctOptionIndex = 0),

        Riddle(question = "The sequence 1, 4, 9, 16, 25 follows a pattern.\nWhat is the 8th term?",
            answer = "64", hint = "Each term is a perfect square: 1²,2²,3²...",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "49|56|64|81", correctOptionIndex = 2),

        Riddle(question = "What letter comes next?\nZ, Y, X, W, V, U, _",
            answer = "T", hint = "The alphabet in reverse.",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "S|T|R|Q", correctOptionIndex = 1),

        Riddle(question = "What is the next number?\n3, 6, 11, 18, 27, _",
            answer = "38", hint = "Look at the differences between consecutive terms.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "36|38|39|40", correctOptionIndex = 1),

        Riddle(question = "Using A=1, B=2 ... Z=26:\nR+E+D = 27.   What does G+R+E+E+N equal?",
            answer = "49", hint = "Sum the letter positions: G=7, R=18, E=5, E=5, N=14.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "45|48|49|52", correctOptionIndex = 2),

        Riddle(question = "Which is the odd one out?\nSQUARE, CIRCLE, TRIANGLE, CUBE",
            answer = "CUBE", hint = "One of these exists in a different number of dimensions.",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "SQUARE|CIRCLE|TRIANGLE|CUBE", correctOptionIndex = 3),

        Riddle(question = "MON = 3 letters. TUE = 3 letters. WED = 3 letters.\nWhat does SAT equal by the same rule?",
            answer = "3", hint = "Count the letters in the abbreviation.",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "3|4|5|6", correctOptionIndex = 0),

        Riddle(question = "What comes next?\n2, 6, 18, 54, _",
            answer = "162", hint = "Each term is multiplied by 3.",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "108|162|180|216", correctOptionIndex = 1),

        Riddle(question = "What is the next number?\n100, 94, 82, 64, 40, _",
            answer = "10", hint = "The differences between terms are increasing by a fixed amount.",
            type = RiddleType.PATTERN.name, difficulty = 3,
            options = "10|12|14|16", correctOptionIndex = 0),

        Riddle(question = "What comes next?\n5, 10, 20, 40, 80, _",
            answer = "160", hint = "Each term doubles.",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "120|140|160|200", correctOptionIndex = 2),

        Riddle(question = "Using A=1, B=2 ... Z=26:\nWhat is the value of G + O + D?",
            answer = "26", hint = "G=7, O=15, D=4.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "24|25|26|27", correctOptionIndex = 2),

        Riddle(question = "Complete the analogy:\nDOG is to PUPPY as CAT is to ___",
            answer = "Kitten", hint = "What is the name for a baby cat?",
            type = RiddleType.PATTERN.name, difficulty = 1,
            options = "Cub|Foal|Kitten|Fawn", correctOptionIndex = 2),

        Riddle(question = "Today is Wednesday. What day of the week will it be in exactly 100 days?",
            answer = "Friday", hint = "100 ÷ 7 = 14 remainder 2. Count 2 days forward from Wednesday.",
            type = RiddleType.PATTERN.name, difficulty = 2,
            options = "Monday|Tuesday|Wednesday|Friday", correctOptionIndex = 3)
    )

    // ═══════════════════════════════════════════════════════════
    //  SEQUENCE  (riddles 41–60)
    // ═══════════════════════════════════════════════════════════
    private val SEQUENCE = listOf(

        Riddle(question = "What is the next number?\n1, 1, 2, 3, 5, 8, 13, 21, _",
            answer = "34", hint = "Each term is the sum of the two before it.",
            type = RiddleType.SEQUENCE.name, difficulty = 1,
            options = "29|34|35|42", correctOptionIndex = 1),

        Riddle(question = "What is the next prime number?\n2, 3, 5, 7, 11, 13, 17, 19, _",
            answer = "23", hint = "List the prime numbers in order.",
            type = RiddleType.SEQUENCE.name, difficulty = 1,
            options = "21|22|23|24", correctOptionIndex = 2),

        Riddle(question = "What comes next in the cubes sequence?\n1, 8, 27, 64, 125, _",
            answer = "216", hint = "1³, 2³, 3³, 4³, 5³, 6³...",
            type = RiddleType.SEQUENCE.name, difficulty = 2,
            options = "196|200|216|225", correctOptionIndex = 2),

        Riddle(question = "Find the next number:\n100, 91, 83, 76, 70, 65, _",
            answer = "61", hint = "The differences between consecutive terms decrease by 1 each time.",
            type = RiddleType.SEQUENCE.name, difficulty = 3,
            options = "59|60|61|62", correctOptionIndex = 2),

        Riddle(question = "What comes next?\n3, 6, 11, 18, 27, 38, _",
            answer = "51", hint = "Differences between terms: 3, 5, 7, 9, 11, 13...",
            type = RiddleType.SEQUENCE.name, difficulty = 2,
            options = "47|49|51|53", correctOptionIndex = 2),

        Riddle(question = "What comes next in this sequence (×2+1)?\n2, 5, 11, 23, 47, _",
            answer = "95", hint = "Multiply each term by 2, then add 1.",
            type = RiddleType.SEQUENCE.name, difficulty = 3,
            options = "93|94|95|96", correctOptionIndex = 2),

        Riddle(question = "What is the next factorial?\n1, 2, 6, 24, 120, _",
            answer = "720", hint = "1!, 2!, 3!, 4!, 5!, 6!",
            type = RiddleType.SEQUENCE.name, difficulty = 2,
            options = "240|360|720|840", correctOptionIndex = 2),

        Riddle(question = "What comes next in this tribonacci sequence?\n0, 1, 1, 2, 4, 7, 13, 24, _",
            answer = "44", hint = "Each term is the sum of the three previous terms.",
            type = RiddleType.SEQUENCE.name, difficulty = 3,
            options = "40|42|44|48", correctOptionIndex = 2),

        Riddle(question = "What is the next number in this arithmetic sequence?\n7, 11, 15, 19, 23, _",
            answer = "27", hint = "The common difference is 4.",
            type = RiddleType.SEQUENCE.name, difficulty = 1,
            options = "25|26|27|28", correctOptionIndex = 2),

        Riddle(question = "What comes next in the powers of 3?\n1, 3, 9, 27, 81, _",
            answer = "243", hint = "3⁰, 3¹, 3², 3³, 3⁴, 3⁵",
            type = RiddleType.SEQUENCE.name, difficulty = 1,
            options = "162|181|243|324", correctOptionIndex = 2),

        Riddle(question = "Find the missing term:\n4, 9, ___, 25, 36",
            answer = "16", hint = "These are squares of consecutive integers.",
            type = RiddleType.SEQUENCE.name, difficulty = 1,
            options = "12|14|16|18", correctOptionIndex = 2),

        Riddle(question = "What comes next (each term = previous × 3 + 1)?\n1, 4, 13, 40, 121, _",
            answer = "364", hint = "1×3+1=4, 4×3+1=13, 13×3+1=40...",
            type = RiddleType.SEQUENCE.name, difficulty = 3,
            options = "243|362|364|366", correctOptionIndex = 2),

        Riddle(question = "What comes next? (Second differences are constant)\n6, 14, 26, 42, 62, _",
            answer = "86", hint = "Differences: 8, 12, 16, 20, 24... each increasing by 4.",
            type = RiddleType.SEQUENCE.name, difficulty = 3,
            options = "80|84|86|90", correctOptionIndex = 2),

        Riddle(question = "What comes next?\n10, 9, 7, 4, 0, _",
            answer = "-5", hint = "Differences: -1, -2, -3, -4, -5...",
            type = RiddleType.SEQUENCE.name, difficulty = 2,
            options = "-3|-4|-5|-6", correctOptionIndex = 2),

        Riddle(question = "What comes next (n³ + 1)?\n2, 9, 28, 65, 126, _",
            answer = "217", hint = "1³+1=2, 2³+1=9, 3³+1=28, 4³+1=65, 5³+1=126, 6³+1=?",
            type = RiddleType.SEQUENCE.name, difficulty = 3,
            options = "196|210|217|224", correctOptionIndex = 2),

        Riddle(question = "What comes next? (Second differences of 2)\n3, 7, 13, 21, 31, 43, _",
            answer = "57", hint = "Differences: 4, 6, 8, 10, 12, 14...",
            type = RiddleType.SEQUENCE.name, difficulty = 3,
            options = "53|55|57|59", correctOptionIndex = 2),

        Riddle(question = "The sequence 2, 4, 8, 16, 32... doubles each time.\nWhat is the 10th term?",
            answer = "1024", hint = "2¹⁰ = 1024",
            type = RiddleType.SEQUENCE.name, difficulty = 2,
            options = "512|1024|2048|256", correctOptionIndex = 1),

        Riddle(question = "What is the next Fibonacci number?\n1, 2, 3, 5, 8, 13, 21, 34, 55, _",
            answer = "89", hint = "Add the last two: 34 + 55",
            type = RiddleType.SEQUENCE.name, difficulty = 1,
            options = "78|83|89|95", correctOptionIndex = 2),

        Riddle(question = "What comes next? (Halving sequence)\n100, 50, 25, 12.5, _",
            answer = "6.25", hint = "Each term is divided by 2.",
            type = RiddleType.SEQUENCE.name, difficulty = 1,
            options = "5|6.25|7.5|10", correctOptionIndex = 1),

        Riddle(question = "What is the sum of the first 10 odd numbers?\n(1 + 3 + 5 + 7 + ... )",
            answer = "100", hint = "The sum of the first n odd numbers always equals n².",
            type = RiddleType.SEQUENCE.name, difficulty = 2,
            options = "55|81|100|110", correctOptionIndex = 2)
    )

    // ═══════════════════════════════════════════════════════════
    //  VISUAL  (riddles 61–80)
    // ═══════════════════════════════════════════════════════════
    private val VISUAL = listOf(

        Riddle(question = "How many squares of ALL sizes are there in a standard 4×4 grid?",
            answer = "30", hint = "Count 1×1, 2×2, 3×3, and 4×4 squares separately, then add.",
            type = RiddleType.VISUAL.name, difficulty = 3,
            options = "16|20|25|30", correctOptionIndex = 3),

        Riddle(question = "A clock shows exactly 3:15. What is the angle between the minute hand and the hour hand?",
            answer = "7.5 degrees", hint = "At 3:00 the angle is 90°. At 3:15 the hour hand has moved 7.5° further.",
            type = RiddleType.VISUAL.name, difficulty = 3,
            options = "0 degrees|7.5 degrees|90 degrees|97.5 degrees", correctOptionIndex = 1),

        Riddle(question = "How many edges does a cube have?",
            answer = "12", hint = "4 edges on top, 4 on bottom, 4 vertical edges connecting them.",
            type = RiddleType.VISUAL.name, difficulty = 1,
            options = "6|8|12|16", correctOptionIndex = 2),

        Riddle(question = "How many faces does a regular tetrahedron have?",
            answer = "4", hint = "A tetrahedron is a triangular pyramid.",
            type = RiddleType.VISUAL.name, difficulty = 1,
            options = "3|4|5|6", correctOptionIndex = 1),

        Riddle(question = "In a 3×3×3 Rubik's cube, how many individual smaller cubes are completely hidden inside (not touching any outer face)?",
            answer = "1", hint = "Only the very center cube is completely internal.",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "0|1|8|27", correctOptionIndex = 1),

        Riddle(question = "How many times do the minute and hour hands of a clock overlap in a 12-hour period?",
            answer = "11", hint = "They don't overlap 12 times — they 'catch up' 11 times.",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "10|11|12|24", correctOptionIndex = 1),

        Riddle(question = "A snail climbs 3 feet up the inside of a 10-foot well each day, but slides back 2 feet each night. How many days does it take to reach the top?",
            answer = "8", hint = "On the 8th day it climbs from 7 feet and reaches 10 — it escapes before nightfall.",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "7|8|9|10", correctOptionIndex = 1),

        Riddle(question = "How many diagonals does a regular hexagon have?",
            answer = "9", hint = "Formula: n(n-3)/2 where n is the number of sides.",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "6|8|9|12", correctOptionIndex = 2),

        Riddle(question = "A Star of David is formed by overlapping two equilateral triangles.\nHow many triangles of ALL sizes can you count in the figure?",
            answer = "8", hint = "Count small (6) + medium (2) triangles.",
            type = RiddleType.VISUAL.name, difficulty = 3,
            options = "6|7|8|12", correctOptionIndex = 2),

        Riddle(question = "What is the maximum number of intersection points created when two distinct circles cross each other?",
            answer = "2", hint = "Two circles can intersect at 0, 1, or 2 points.",
            type = RiddleType.VISUAL.name, difficulty = 1,
            options = "1|2|3|4", correctOptionIndex = 1),

        Riddle(question = "A rectangle has a perimeter of 24 cm. What dimensions give the maximum possible area?",
            answer = "6×6", hint = "A square maximizes area for a given perimeter.",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "4×8|5×7|6×6|3×9", correctOptionIndex = 2),

        Riddle(question = "What is the maximum number of intersection points when 3 distinct circles overlap each other?",
            answer = "6", hint = "Each pair of circles can produce at most 2 intersections. 3 pairs × 2 = ?",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "3|6|9|12", correctOptionIndex = 1),

        Riddle(question = "How many vertices (corners) does a triangular prism have?",
            answer = "6", hint = "Two triangular faces × 3 vertices each.",
            type = RiddleType.VISUAL.name, difficulty = 1,
            options = "4|5|6|8", correctOptionIndex = 2),

        Riddle(question = "On a standard six-sided die, opposite faces always sum to 7.\nWhich face is directly opposite the face showing 5?",
            answer = "2", hint = "If opposite faces sum to 7: 5 + ? = 7",
            type = RiddleType.VISUAL.name, difficulty = 1,
            options = "1|2|3|4", correctOptionIndex = 1),

        Riddle(question = "A standard domino set contains all combinations of numbers from 0 to 6.\nHow many dominoes are in the complete set?",
            answer = "28", hint = "Formula: (n+1)(n+2)/2 where n=6. That gives 7×8/2.",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "21|24|28|36", correctOptionIndex = 2),

        Riddle(question = "How many sides does a heptagon have?",
            answer = "7", hint = "'Hepta' is the Greek prefix for seven.",
            type = RiddleType.VISUAL.name, difficulty = 1,
            options = "5|6|7|8", correctOptionIndex = 2),

        Riddle(question = "What is the minimum number of straight cuts needed to divide a circle into 8 equal sectors?",
            answer = "4", hint = "Each cut through the center produces 2 sectors. 4 cuts through center = 8 sectors.",
            type = RiddleType.VISUAL.name, difficulty = 2,
            options = "3|4|8|7", correctOptionIndex = 1),

        Riddle(question = "A chess knight starts on square a1. What is the minimum number of moves to reach h8?",
            answer = "6", hint = "Knight path: a1→c2→e3→g4→h6→f7→h8 (one of many 6-move solutions).",
            type = RiddleType.VISUAL.name, difficulty = 3,
            options = "4|5|6|7", correctOptionIndex = 2),

        Riddle(question = "When you unfold (create the net of) a cube, how many faces appear in the flat pattern?",
            answer = "6", hint = "A cube has 6 faces. The net unfolds all of them.",
            type = RiddleType.VISUAL.name, difficulty = 1,
            options = "4|5|6|8", correctOptionIndex = 2),

        Riddle(question = "A triangle has two sides of length 7 and 10. Which statement correctly describes the possible range of the third side (c)?",
            answer = "3 to 17", hint = "Triangle inequality: |a-b| < c < a+b → |7-10| < c < 7+10",
            type = RiddleType.VISUAL.name, difficulty = 3,
            options = "3 to 17|1 to 17|5 to 15|0 to 17", correctOptionIndex = 0)
    )

    // ═══════════════════════════════════════════════════════════
    //  DEDUCTION  (riddles 81–100)
    // ═══════════════════════════════════════════════════════════
    private val DEDUCTION = listOf(

        Riddle(question = "Monty Hall Problem: You pick Door 1. The host opens Door 3 to reveal a goat. What gives you the BEST chance of winning the car?",
            answer = "Switch to Door 2", hint = "Switching wins 2/3 of the time. Staying wins only 1/3.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "Stay with Door 1|Switch to Door 2|It doesn't matter|Open Door 3", correctOptionIndex = 1),

        Riddle(question = "A drawer has 10 red socks and 10 blue socks mixed randomly in the dark.\nWhat is the minimum number of socks you must draw to guarantee a matching pair?",
            answer = "3", hint = "Worst case: draw one of each color. The third MUST match one.",
            type = RiddleType.DEDUCTION.name, difficulty = 2,
            options = "2|3|10|11", correctOptionIndex = 1),

        Riddle(question = "You have a 3-liter jar and a 5-liter jar. How do you measure exactly 4 liters?",
            answer = "Fill 5L, pour into 3L, empty 3L, pour 2L into 3L, fill 5L, top up 3L — 4L left in 5L",
            hint = "Fill the 5L jar, pour off 3L, empty the 3L jar, pour the remaining 2L in, then refill the 5L and top up the 3L.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "Fill 3L twice into 5L — impossible|Fill 5L, pour into 3L, empty 3L, pour 2L into 3L, fill 5L, top up 3L — 4L left in 5L|You cannot do it|Fill 5L and remove 1L", correctOptionIndex = 1),

        Riddle(question = "Logical syllogism:\n'All men are mortal. Socrates is a man.'\nWhich conclusion is definitely true?",
            answer = "Socrates is mortal", hint = "Apply modus ponens: if all A are B, and X is A, then X is B.",
            type = RiddleType.DEDUCTION.name, difficulty = 1,
            options = "All mortals are men|Socrates is mortal|All men are Socrates|Mortals are human", correctOptionIndex = 1),

        Riddle(question = "In a village of truth-tellers and liars, a person says: 'I am a liar.'\nWhat can you conclude?",
            answer = "It's a paradox — neither is possible", hint = "A truth-teller can't say it (would be lying). A liar can't say it truthfully.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "Truth-teller|Liar|It's a paradox — neither is possible|Both", correctOptionIndex = 2),

        Riddle(question = "Four people need to cross a bridge at night with one torch. They walk at 1, 2, 5, and 10 min. Max 2 can cross at once and must carry the torch. What is the minimum time for all to cross?",
            answer = "17", hint = "Send 1&2 (2), return 1 (1), send 5&10 (10), return 2 (2), send 1&2 (2). Total = 17.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "15|17|19|20", correctOptionIndex = 1),

        Riddle(question = "A man has exactly 2 children. You are told that one is a boy born on a Tuesday.\nWhat is the probability that both children are boys?",
            answer = "13/27", hint = "This is a conditional probability problem. Use the sample space of (gender, day-of-birth) pairs.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "1/2|1/3|13/27|1/4", correctOptionIndex = 2),

        Riddle(question = "In the classic Prisoner's Dilemma, what is the dominant strategy for a purely self-interested rational player?",
            answer = "Confess", hint = "Confessing dominates staying silent regardless of what the other prisoner does.",
            type = RiddleType.DEDUCTION.name, difficulty = 2,
            options = "Stay silent|Confess|Match the other prisoner|Flip a coin", correctOptionIndex = 1),

        Riddle(question = "If it takes 10 men 10 days to dig 10 holes, how long does it take 1 man to dig half a hole?",
            answer = "There is no such thing as half a hole", hint = "A hole is either dug or it isn't — 'half a hole' is meaningless.",
            type = RiddleType.DEDUCTION.name, difficulty = 2,
            options = "5 days|0.5 days|100 days|There is no such thing as half a hole", correctOptionIndex = 3),

        Riddle(question = "Wason Selection Task:\nRule: 'If a card has a vowel on one side, it has an even number on the other.'\nCards visible: A, K, 4, 7. Which cards MUST you turn over to test the rule?",
            answer = "A and 7", hint = "You must check: cards that could falsify the rule. A vowel must have an even number. An odd number must NOT have a vowel.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "A and 4|A only|A and 7|A, 4, and 7", correctOptionIndex = 2),

        Riddle(question = "A box contains red and blue marbles. Remove 2 red → P(red) = 1/4. Remove 2 blue → P(red) = 1/2.\nHow many marbles are originally in the box?",
            answer = "10", hint = "Set up two equations with r (red) and b (blue) and solve simultaneously.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "8|10|12|16", correctOptionIndex = 1),

        Riddle(question = "A man runs into a restaurant and asks for a glass of water. The waiter points a gun at him. The man says 'Thank you' and walks out calmly. Why?",
            answer = "The fright cured his hiccups", hint = "The man had a common, harmless physical ailment that fright can cure.",
            type = RiddleType.DEDUCTION.name, difficulty = 2,
            options = "He was a criminal|He didn't really want water|The fright cured his hiccups|He was testing the waiter", correctOptionIndex = 2),

        Riddle(question = "Birthday Paradox: How many people must be in a room for there to be a greater than 50% probability that two of them share the same birthday?",
            answer = "23", hint = "It's much less than 183 (half of 365). The probability grows faster than intuition suggests.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "50|40|23|183", correctOptionIndex = 2),

        Riddle(question = "A coin has been tossed and landed heads 5 times in a row.\nWhat is the probability it lands heads on the 6th toss?",
            answer = "50%", hint = "Each toss is an independent event. Previous results don't affect future ones.",
            type = RiddleType.DEDUCTION.name, difficulty = 1,
            options = "Less than 50%|50%|More than 50%|Depends on the coin", correctOptionIndex = 1),

        Riddle(question = "At a party, everyone shakes hands exactly once with every other person.\nIf there are 45 handshakes total, how many people are at the party?",
            answer = "10", hint = "Formula: n(n-1)/2 = 45. Solve for n.",
            type = RiddleType.DEDUCTION.name, difficulty = 2,
            options = "8|9|10|12", correctOptionIndex = 2),

        Riddle(question = "A car travels the first 30 miles of a 60-mile trip at 30 mph.\nHow fast must it travel the second 30 miles to average 60 mph for the whole trip?",
            answer = "Impossible", hint = "At 30 mph for 30 miles, 60 min is already used. 60 mph over 60 miles also requires exactly 60 min — all time is spent.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "90 mph|120 mph|Impossible|60 mph", correctOptionIndex = 2),

        Riddle(question = "There are 3 light switches outside a room. Inside is one light bulb. You may enter the room only once. How do you determine which switch controls the bulb?",
            answer = "Turn S1 on for 10 min, off. Turn S2 on. Enter — on=S2, warm&off=S1, cold&off=S3",
            hint = "Use both electricity AND heat as information sources.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "Turn each on briefly and check|Turn on all three at once|Turn S1 on 10 min, off. Turn S2 on. Enter — on=S2, warm bulb=S1, cold=S3|You need to enter twice", correctOptionIndex = 2),

        Riddle(question = "You face two doors: one to freedom, one to death. Two guards — one always lies, one always tells truth. You may ask ONE guard ONE question. What do you ask?",
            answer = "Ask either: 'What would the OTHER guard say leads to freedom?' Then go the OPPOSITE door",
            hint = "Both guards will point to the WRONG door when asked what the other would say.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "Ask if they are honest|Ask which door is safe|Ask either: 'What would the OTHER guard say leads to freedom?' Then go the OPPOSITE door|Guess randomly", correctOptionIndex = 2),

        Riddle(question = "Bertrand's Box Paradox:\n3 boxes: one has 2 gold coins, one has 2 silver, one has 1 gold + 1 silver (all labels wrong).\nYou draw one gold coin from a random box. What is the probability the OTHER coin in that box is also gold?",
            answer = "2/3", hint = "There are 3 gold coins you could have drawn. Two of them are in the GG box. Use conditional probability.",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "1/2|2/3|1/3|3/4", correctOptionIndex = 1),

        Riddle(question = "5 pirates must divide 100 gold coins. The most senior proposes a split; if >50% reject it, the proposer is thrown overboard and the next senior proposes. How many coins does the senior pirate keep (assuming all are perfectly rational)?",
            answer = "97", hint = "Work backwards from 2 pirates. Senior only needs to bribe the 2 cheapest votes: pirate 3 (needs 1) and pirate 4 (needs 2).",
            type = RiddleType.DEDUCTION.name, difficulty = 3,
            options = "85|90|97|100", correctOptionIndex = 2)
    )
}
