import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe


class Day01Part1: BehaviorSpec() { init {
    val exampleInput = """
    1abc2
    pqr3stu8vwx
    a1b2c3d4e5f
    treb7uchet
    """.trimIndent()

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

class Day01Part2: BehaviorSpec() { init {

    Given("a string") {
        val str = "abcone2threexyz"
        When("searching first") {
            val search = setOf("one")
            val found = findFirstOf(str, search)
            Then("it should find the value") {
                found shouldBe "one"
            }
        }
        When("searching and parsing first") {
            val found = parseFirstDigit(str, true)
            Then("it should find the value") {
                found shouldBe 1
            }
        }
        When("searching last") {
            val search = setOf("three")
            val found = findLastOf(str, search)
            Then("it should find the value") {
                found shouldBe "three"
            }
        }
        When("searching and parsing last") {
            val found = parseLastDigit(str, true)
            Then("it should find the value") {
                found shouldBe 3
            }
        }
    }
    Given("another sting") {
        val str = "6nfhcklxlkg9jbqmqrrxmhn9two6"
        When("searching and parsing first") {
            val found = parseFirstDigit(str)
            Then("it should find the value") {
                found shouldBe 6
            }
        }
        When("searching and parsing last") {
            val found = parseLastDigit(str)
            Then("it should find the value") {
                found shouldBe 6
            }
        }
    }
    Given("example input") {
        val exampleInput = """
            two1nine
            eightwothree
            abcone2threexyz
            xtwone3four
            4nineeightseven2
            zoneight234
            7pqrstsixteen
            """.trimIndent()
        val calibrations = parseCalibrations(exampleInput, true)
        Then("should have found 7 calibrations") {
            calibrations.size shouldBe 7
        }
        Then("first calibration should be parsed correctly") {
            calibrations[0] shouldBe 29
        }
        When("calculating the sum of calibrations") {
            val sum = calibrations.sum()
            Then("sum should be right") {
                sum shouldBe 281
            }
        }
    }
    Given("exercise input") {
        val calibrations = parseCalibrations(readResource("inputDay01.txt")!!, true)
        When("calculating the sum of calibrations") {
            val sum = calibrations.sum()
            Then("it should find the solution for part 2") {
                sum shouldBeLessThan  55_319
                sum shouldBe 55_260
            }
        }
    }
} }

fun parseCalibrations(input: String, includeSpelled: Boolean = false) = input.split("\n").map { line ->
    val firstDigit = parseFirstDigit(line, includeSpelled)
    val lastDigit = parseLastDigit(line, includeSpelled)
    "$firstDigit$lastDigit".toInt()
}

fun findFirstOf(str: String, searchList: Set<String>) =
    searchList.map { it to str.indexOf(it) } // Pair of search string and pos, -1 if not found
        .filter { it.second >= 0} // only search items which are occuring
        .minBy { it.second } // the first
        .first // return only the string no the index
fun findLastOf(str: String, searchList: Set<String>) =
    searchList.map { it to str.lastIndexOf(it) } // Pair of search string and pos, -1 if not found
        .filter { it.second >= 0} // only search items which are occuring
        .maxBy { it.second } // the last
        .first // return only the string no the index

val converterMap = mapOf(
    "one" to 1,
    "two" to 2,
    "three" to 3,
    "four" to 4,
    "five" to 5,
    "six" to 6,
    "seven" to 7,
    "eight" to 8,
    "nine" to 9)
val digitMap = (1..9).associateBy { it.toString() }
val completeConverterMap = converterMap + digitMap

fun parseFirstDigit(str: String, includeSpelled: Boolean = false): Int {
    val found = findFirstOf(str, if (includeSpelled) completeConverterMap.keys else digitMap.keys)
    return completeConverterMap[found] ?: throw IllegalArgumentException()
}

fun parseLastDigit(str: String, includeSpelled: Boolean = false): Int {
    val found = findLastOf(str, if (includeSpelled) completeConverterMap.keys else digitMap.keys)
    return completeConverterMap[found] ?: throw IllegalArgumentException()
}