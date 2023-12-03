import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe



class Day03Part1: BehaviorSpec() { init {

    Given("example input") {
        val exampleInput = """
            467..114..
            ...*......
            ..35..633.
            ......#...
            617*......
            .....+.58.
            ..592.....
            ......755.
            ...${'$'}.*....
            .664.598..
        """.trimIndent()
        When("parsing the input") {
            val engineSchematic = parseEngineSchematic(exampleInput)
            Then("should have found right y coords") {
                engineSchematic.size shouldBe 10
            }
            Then("should have found right x coords") {
                engineSchematic.forEach() { it.size  shouldBe 10 }
            }
            When("checking for adjacent symbols") {
                checkForAdjacentSymbol(engineSchematic, 0, 0) shouldBe false
                checkForAdjacentSymbol(engineSchematic, 2, 0) shouldBe true
                checkForAdjacentSymbol(engineSchematic, 2, 4) shouldBe true
            }
            When("scanning for part numbers") {
                val numbers = scanPartNumbers(engineSchematic)
                Then("it should have found the first number") {
                    numbers shouldContain 467
                }
                Then("it should have excluded non parts") {
                    numbers shouldNotContain 114
                    numbers shouldNotContain 58
                }
                Then("it should have found the right sum") {
                    numbers.sum() shouldBe 4361
                }
            }
        }
    }

    Given("exercise input") {
        val engineSchematic = parseEngineSchematic(readResource("inputDay03.txt")!!)
        When("finding the parts") {
            val parts = scanPartNumbers(engineSchematic)
            Then("it should have the right sum") {
                parts.sum() shouldBeLessThan 563793
                parts.sum() shouldBeGreaterThan 366076
                parts.sum() shouldBe 531932
            }
        }
    }

} }

private fun parseEngineSchematic(input: String): EngineSchematic = input.split("\n").map { it.toCharArray().asList() }

private fun scanPartNumbers(engineSchematic: List<List<Char>>): List<Int> {
    val result = mutableListOf<Int>()
    var currentDigits = mutableListOf<Char>()
    var adjacentSymbolFound = false
    for(y in 0 until engineSchematic.size) {
        for (x in 0 until engineSchematic[y].size) {
            val currentChar = engineSchematic[y][x]
            if (currentChar.isDigit()) {
                currentDigits += currentChar
                if (checkForAdjacentSymbol(engineSchematic, x, y)) // During collecting digits must check for adjacent symbols
                    adjacentSymbolFound = true
            } else if (currentDigits.size > 0) { // was collecting digits for a number and this ended
                if (adjacentSymbolFound) // add only numbers with adjacent symbol
                    result += currentDigits.joinToString("").toInt()
                currentDigits = mutableListOf<Char>()
                adjacentSymbolFound = false
            }
        }
        // handle end of line
        if (currentDigits.size > 0) { // was collecting digits for a number and this ended
            if (adjacentSymbolFound) // add only numbers with adjacent symbol
                result += currentDigits.joinToString("").toInt()
        }
        currentDigits = mutableListOf<Char>()
        adjacentSymbolFound = false
    }
    return result
}
private fun checkForAdjacentSymbol(engineSchematic: EngineSchematic, x: Int, y: Int): Boolean {
    for (dx in -1 .. 1)
        for (dy in -1 .. 1)
            if (! (dx == 0 && dy == 0)) {
                val char = engineSchematic.getOrNull(y + dy)?.getOrNull(x + dx)
                if (char != null && char != '.' && !char.isDigit()) return true
            }
    return false
}

typealias EngineSchematic = List<List<Char>>
