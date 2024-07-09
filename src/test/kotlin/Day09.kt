import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInputDay09 = """
    0 3 6 9 12 15
    1 3 6 10 15 21
    10 13 16 21 30 45
    """.trimIndent()

class Day09Part1: BehaviorSpec() { init {
    Given("example input") {
        When("parsing the input") {
            val histories = parseHistories(exampleInputDay09)
            Then("histories should be parsed correctly") {
                histories.size shouldBe 3
                histories[0] shouldBe listOf(0, 3, 6, 9, 12, 15)
            }
            When("finding prediction for first example") {
                val prediction = findPrediction(histories[0])
                Then("prediction should be right") {
                    prediction shouldBe 18
                }
            }
            When("finding prediction for all examples") {
                val predictions = findPredictions(histories)
                Then("predictions should be right") {
                    predictions shouldBe listOf(18, 28, 68)
                }
            }
        }
    }
    Given("exercise input") {
        val histories = parseHistories(readResource("inputDay09.txt")!!)
        histories.size shouldBe 200
        When("finding prediction for all input lines and summing") {
            val predictions = findPredictions(histories).sum()
            Then("it should have the result") {
                predictions shouldBe 1_782_868_781
            }
        }
    }
} }

fun parseHistories(input: String) = input.split("\n").map { line ->
    line.trim().split(" ").map {
        it.toInt()
    }
}

fun findPrediction(list: List<Int>): Int =
    sequence {
        var curr = list
        do {
            yield(curr)
            curr = curr.zip(curr.drop(1)).map { (left, right) -> right - left}
        } while (curr.any { it != 0} )
    }.sumOf { it.last() }

fun findPredictions(sequences: List<List<Int>>) = sequences.map { findPrediction(it) }
