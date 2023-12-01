import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

val exampleInput = """
    1abc2
    pqr3stu8vwx
    a1b2c3d4e5f
    treb7uchet
    """.trimIndent()

class Day01Part1: BehaviorSpec() { init {
    Given("example input") {
        val calibrations = parseCalibrations(exampleInput)
        Then("should have found 4 calibrations") {
            calibrations.size shouldBe 4
        }
        Then("first calibration should be parsed correctly") {
            calibrations[0] shouldBe 12
        }
        Then("and third") {
            calibrations[2] shouldBe 15
        }
        When("calculating the sum of calibrations") {
            val sum = calibrations.sum()
            Then("sum should be right") {
                sum shouldBe 142
            }
        }
    }
    Given("exercise input") {
        val calibrations = parseCalibrations(readResource("inputDay01.txt")!!)
        When("calculating the sum of calibrations") {
            val sum = calibrations.sum()
            Then("it should find the solution for part 1") {
                sum shouldBe 55_123
            }
        }
    }
} }

fun parseCalibrations(input: String) = input.split("\n").map { line ->
    val firstDigit = line.first { it.isDigit() }
    val lastDigit = line.last { it.isDigit() }
    "$firstDigit$lastDigit".toInt()
}
