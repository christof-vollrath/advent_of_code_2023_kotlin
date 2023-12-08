import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.math.pow


val exampleInputDay04 = """
        Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
        Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
        Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
        Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
        Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
        Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
    """.trimIndent()

class Day04Part1: BehaviorSpec() { init {

    Given("example input") {
        When("parsing the input") {
            val scratchCards = parseScratchCards(exampleInputDay04)
            Then("should have parsed all cards") {
                scratchCards.size shouldBe 6
            }
            Then("should have found right first scratch card") {
                scratchCards[0].id shouldBe 1
                scratchCards[0].winningNumbers shouldBe setOf(41, 48, 83, 86, 17)
                scratchCards[0].yourNumbers shouldBe setOf(83, 86, 6, 31, 17, 9, 48, 53)
            }
            When("finding matching numbers") {
                val matchingNumbers = findMatchingNumbers(scratchCards[0].winningNumbers, scratchCards[0].yourNumbers)
                Then("it should have found the intersection between winning and your numbers") {
                    matchingNumbers shouldBe setOf(48, 83, 17, 86)
                }
            }
            When("calculating worth of your numbers") {
                val worth = calculateWorth(scratchCards[0].winningNumbers, scratchCards[0].yourNumbers)
                Then("it should calculate the right worth") {
                    worth shouldBe 8
                }
            }
            When("calculating the sum of all worths") {
                val sum = scratchCards.sumOf { it.worth } // worth should be already set in scratchard
                Then("it should calculate the right sum") {
                    sum shouldBe 13
                }
            }
        }
    }

    Given("exercise input") {
        val scratchCards = parseScratchCards(readResource("inputDay04.txt")!!)
        When("summing worth") {
            val sum = scratchCards.sumOf { it.worth }
            Then("it should have the right sum") {
                sum shouldBe 19135
            }
        }
    }
} }

class Day04Part2: BehaviorSpec() { init {

    Given("example input") {
        val scratchCards = parseScratchCards(exampleInputDay04)
        val scratchCardsWithNumber = scratchCards.map { ScratchCardWithNumber(it, 1) }
        When("playing stretch game") {
            playScretchCards(scratchCardsWithNumber)
            Then("card 1 should have nr 1") {
                scratchCardsWithNumber[0].nr shouldBe 1
            }
            Then("card 2 should have nr 2") {
                scratchCardsWithNumber[1].nr shouldBe 2
            }
            Then("card 3 should have nr 4") {
                scratchCardsWithNumber[2].nr shouldBe 4
            }
            When("summing all card numbers after play is finished") {
                Then("it should have calculated the right sum") {
                    scratchCardsWithNumber.sumOf { it.nr } shouldBe 30
                }
            }
        }
    }

    Given("exercise input") {
        val scratchCards = parseScratchCards(readResource("inputDay04.txt")!!)
        val scratchCardsWithNumber = scratchCards.map { ScratchCardWithNumber(it, 1) }
        playScretchCards(scratchCardsWithNumber)
        When("summing all card numbers after play is finished") {
            Then("it should have calculated the right sum") {
                scratchCardsWithNumber.sumOf { it.nr } shouldBe 5704953
            }
        }
    }
} }

fun playScretchCards(scratchCardsWithNumber: List<ScratchCardWithNumber>) {
    for (i1 in scratchCardsWithNumber.indices) {
        val (scratchCard, nr) = scratchCardsWithNumber[i1]
        for (i2 in 1..scratchCard.nrMatching) {
            if (i1 + i2 < scratchCardsWithNumber.size)
                scratchCardsWithNumber[i1 + i2].nr += nr
        }
    }
}

fun findMatchingNumbers(winningNumbers: Set<Int>, yourNumbers: Set<Int>) = winningNumbers intersect yourNumbers

fun calculateWorth(winningNumbers: Set<Int>, yourNumbers: Set<Int>): Int {
    val nrMatching = findMatchingNumbers(winningNumbers, yourNumbers).size
    return if (nrMatching <= 1) nrMatching
    else 2.0.pow((nrMatching - 1).toDouble()).toInt()
}
fun parseScratchCards(input: String) = input.split("\n").map { parseScratchCard(it.trim()) }

fun parseScratchCard(line: String): ScratchCard {
    val regex = """Card +(\d+): ([\d ]+) \| ([\d ]+)""".toRegex()
    val match = regex.find(line.trim()) ?: throw IllegalArgumentException("Can not parse input=$line")
    if (match.groupValues.size != 4) throw IllegalArgumentException("Wrong number of elements parsed")
    val id = match.groupValues[1].toInt()
    val winningNumbers = match.groupValues[2].split(" ").filter { it.isNotBlank() }.map { it.trim().toInt() }
    val yourNumbers = match.groupValues[3].split(" ").filter { it.isNotBlank() }.map { it.trim().toInt() }
    return ScratchCard(id, winningNumbers.toSet(), yourNumbers.toSet())
}

data class ScratchCard(val id: Int, val winningNumbers: Set<Int>, val yourNumbers: Set<Int>, val nrMatching: Int = findMatchingNumbers(winningNumbers, yourNumbers).size) {
    val worth: Int
        get() = if (nrMatching <= 1) nrMatching
        else 2.0.pow((nrMatching - 1).toDouble()).toInt()
}

data class ScratchCardWithNumber(val scratchCard: ScratchCard, var nr: Int)